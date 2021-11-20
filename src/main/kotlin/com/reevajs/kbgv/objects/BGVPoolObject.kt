package com.reevajs.kbgv.objects

import com.reevajs.kbgv.*
import kotlinx.serialization.json.*
import java.util.*

/**
 *     PoolObject {
 *         union {
 *             NullPoolObject
 *             NewPoolObject
 *             ReferencePoolObject
 *         }
 *     }
 */
sealed interface IBGVPoolObject : IBGVObject {
    companion object : IBGVReader<IBGVPoolObject> {
        private val ALLOWED_REF_TYPES = setOf(
            BGVToken.POOL_STRING,
            BGVToken.POOL_ENUM,
            BGVToken.POOL_CLASS,
            BGVToken.POOL_METHOD,
            BGVToken.POOL_NODE_CLASS,
            BGVToken.POOL_FIELD,
            BGVToken.POOL_SIGNATURE,
            BGVToken.POOL_NODE_SOURCE_POSITION,
            BGVToken.POOL_NODE,
        )

        override fun read(reader: ExpandingByteBuffer, context: Context): IBGVPoolObject {
            return when (val type = reader.getByte()) {
                BGVToken.POOL_NULL -> BGVNullPool
                BGVToken.POOL_NEW -> BGVNewPool.read(reader, context)
                else -> {
                    expect(type in ALLOWED_REF_TYPES)
                    context[reader.getShort().toUShort()]
                }
            }
        }
    }
}

/**
 *     NullPoolObject {
 *         sint8 token = POOL_NEW
 *     }
 */
object BGVNullPool : IBGVPoolObject {
    override fun write(writer: ExpandingByteBuffer, context: Context) {
        writer.putByte(BGVToken.POOL_NULL)
    }

    override fun toJson(context: Context) = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "null")
    }

    override fun toString() = "NullPool"
}

/**
 *     NewPoolObject {
 *         sint8 token = POOL_NEW
 *         uint16 id
 *         union {
 *             StringPoolObject
 *             EnumPoolObject
 *             ClassPoolObject
 *             MethodPoolObject
 *             NodeClassPoolObject
 *             FieldPoolObject
 *             NodeSignaturePoolObject
 *             NodeSourcePositionPoolObject
 *             NodePoolObject
 *         }
 *     }
 */
sealed class BGVNewPool(id: UShort, token: Byte) : IBGVPoolObject {
    var id: UShort = id
        internal set
    var token: Byte = token
        internal set

    override fun write(writer: ExpandingByteBuffer, context: Context) {
        if (id in context) {
            // Write a reference
            writer.putByte(token)
            writer.putShort(id.toShort())
        } else {
            context[id] = this
            writeImpl(writer, context)
        }
    }

    open fun writeImpl(writer: ExpandingByteBuffer, context: Context) {
        writer.putByte(BGVToken.POOL_NEW)
        writer.putShort(id.toShort())
        writer.putByte(token)
    }

    override fun toJson(context: Context): JsonElement {
        return if (id in context) {
            // Write a reference
            buildJsonObject {
                put("\$type", "\$ref")
                put("\$ref", JsonPrimitive(id.toInt()))
            }
        } else toJsonImpl(context).also {
            context[id] = this
        }
    }

    abstract fun toJsonImpl(context: Context): JsonElement

    companion object : IBGVReader<BGVNewPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNewPool {
            val id = reader.getShort().toUShort()
            val token = reader.getByte()

            return when (token) {
                BGVToken.POOL_STRING -> BGVStringPool.read(reader, context)
                BGVToken.POOL_ENUM -> BGVEnumPool.read(reader, context)
                BGVToken.POOL_CLASS -> BGVClassPool.read(reader, context)
                BGVToken.POOL_METHOD -> BGVMethodPool.read(reader, context)
                BGVToken.POOL_NODE_CLASS -> BGVNodeClassPool.read(reader, context)
                BGVToken.POOL_FIELD -> BGVFieldPool.read(reader, context)
                BGVToken.POOL_SIGNATURE -> BGVNodeSignaturePool.read(reader, context)
                BGVToken.POOL_NODE_SOURCE_POSITION -> BGVNodeSourcePositionPool.read(reader, context)
                BGVToken.POOL_NODE -> BGVNodePool.read(reader, context)
                else -> unreachable()
            }.also {
                it.id = id
                it.token = token
                context[id] = it
            }
        }
    }
}

/**
 *     StringPoolObject {
 *         sint8 type = POOL_STRING
 *         String string
 *     }
 */
class BGVStringPool(id: UShort, val string: String) : BGVNewPool(id, BGVToken.POOL_STRING) {
    override fun writeImpl(writer: ExpandingByteBuffer, context: Context) {
        super.writeImpl(writer, context)
        writer.putString(string)
    }

    override fun toJsonImpl(context: Context) = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "string")
        put("id", id.toInt())
        put("value", string)
    }

    override fun toString() = "StringPool {$string}"

    companion object : IBGVReader<BGVStringPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVStringPool {
            return BGVStringPool(0U, reader.getString())
        }
    }
}


/**
 *     EnumPoolObject {
 *         sint8 type = POOL_ENUM
 *         PoolObject enum_class
 *         sint32 enum_ordinal
 *     }
 */
class BGVEnumPool(
    id: UShort,
    val enumClass: BGVClassPool,
    val ordinal: Int,
) : BGVNewPool(id, BGVToken.POOL_ENUM) {
    override fun writeImpl(writer: ExpandingByteBuffer, context: Context) {
        super.writeImpl(writer, context)
        enumClass.write(writer, context)
        writer.putInt(ordinal)
    }

    override fun toJsonImpl(context: Context) = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "enum")
        put("id", id.toInt())
        put("enum_class", enumClass.toJson(context))
        put("ordinal", ordinal)
    }

    override fun toString() = "EnumPool {class=$enumClass, ordinal=$ordinal}"

    companion object : IBGVReader<BGVEnumPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVEnumPool {
            val enumClass = IBGVPoolObject.read(reader, context)
            expectIs<BGVClassPool>(enumClass)
            return BGVEnumPool(0U, enumClass, reader.getInt())
        }
    }
}

/**
 *     ClassPoolType {
 *         union {
 *             KlassType
 *             EnumKlassType
 *         }
 *     }
 */
sealed interface IBGVClassPoolType : IBGVObject

/**
 *     EnumKlassType {
 *         sint8 type = ENUM_KLASS
 *         sint32 values_count
 *         PoolObject[values_count] values
 *     }
 */
class BGVClassPoolEnumType(val values: List<BGVStringPool>) : IBGVClassPoolType {
    override fun write(writer: ExpandingByteBuffer, context: Context) {
        writer.putByte(BGVToken.ENUM_KLASS)
        writer.putInt(values.size)
        values.forEach {
            it.write(writer, context)
        }
    }

    override fun toJson(context: Context) = buildJsonObject {
        put("\$type", "class_pool_type")
        put("class_type", "enum_klass")
        putJsonArray("values") {
            values.forEach { add(it.toJson(context)) }
        }
    }

    override fun toString() = "ENUM_KLASS"

    companion object : IBGVReader<BGVClassPoolEnumType> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVClassPoolEnumType {
            val length = reader.getInt()
            val values = (0 until length).map {
                val value = IBGVPoolObject.read(reader, context)
                expectIs<BGVStringPool>(value)
                value
            }
            return BGVClassPoolEnumType(values)
        }
    }
}

/**
 *     KlassType {
 *         sint8 token = KLASS
 *     }
 */
object BGVClassPoolKlassType : IBGVClassPoolType {
    override fun write(writer: ExpandingByteBuffer, context: Context) {
        writer.putByte(BGVToken.KLASS)
    }

    override fun toJson(context: Context) = buildJsonObject {
        put("\$type", "class_pool_type")
        put("class_type", "klass")
    }

    override fun toString() = "KLASS"
}

/**
 *     ClassPoolObject {
 *         sint8 type = POOL_CLASS
 *         String type_name
 *         ClassPoolType type
 *     }
 */
class BGVClassPool(
    id: UShort,
    val typeName: String,
    val type: IBGVClassPoolType,
) : BGVNewPool(id, BGVToken.POOL_CLASS) {
    override fun writeImpl(writer: ExpandingByteBuffer, context: Context) {
        super.writeImpl(writer, context)
        writer.putString(typeName)
        type.write(writer, context)
    }

    override fun toJsonImpl(context: Context) = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "class")
        put("id", id.toInt())
        put("type_name", typeName)
        put("type", type.toJson(context))
    }

    override fun toString() = "ClassPool {name=$typeName, type=$type}"

    companion object : IBGVReader<BGVClassPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVClassPool {
            val typeName = reader.getString()
            val type = when (reader.getByte()) {
                BGVToken.KLASS -> BGVClassPoolKlassType
                BGVToken.ENUM_KLASS -> BGVClassPoolEnumType.read(reader, context)
                else -> unreachable()
            }
            return BGVClassPool(0U, typeName, type)
        }
    }
}

/**
 *    MethodPoolObject {
 *        sint8 type = POOL_METHOD
 *        PoolObject declaring_class
 *        PoolObject method_name
 *        PoolObject signature
 *        sint32 modifiers
 *        sint32 bytes_length
 *        uint8[bytes_length] bytes
 *    }
 */
class BGVMethodPool(
    id: UShort,
    val declaringClass: BGVClassPool,
    val methodName: BGVStringPool,
    val signature: BGVNodeSignaturePool,
    val modifiers: Int,
    val bytes: ByteArray,
) : BGVNewPool(id, BGVToken.POOL_METHOD) {
    override fun writeImpl(writer: ExpandingByteBuffer, context: Context) {
        super.writeImpl(writer, context)
        declaringClass.write(writer, context)
        methodName.write(writer, context)
        signature.write(writer, context)
        writer.putInt(modifiers)
        writer.putBytes(bytes)
    }

    override fun toJsonImpl(context: Context) = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "method")
        put("id", id.toInt())
        put("declaring_class", declaringClass.toJson(context))
        put("method_name", methodName.toJson(context))
        put("signature", signature.toJson(context))
        put("modifiers", modifiers)

        val byteString = Base64.getEncoder().encodeToString(bytes)
        put("bytes", byteString)
    }

    override fun toString() = "MethodPool {name=$methodName, sig=$signature, class=$declaringClass}"

    companion object : IBGVReader<BGVMethodPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVMethodPool {
            val declaringClass = IBGVPoolObject.read(reader, context)
            val methodName = IBGVPoolObject.read(reader, context)
            val signature = IBGVPoolObject.read(reader, context)

            expectIs<BGVClassPool>(declaringClass)
            expectIs<BGVStringPool>(methodName)
            expectIs<BGVNodeSignaturePool>(signature)

            return BGVMethodPool(
                0U,
                declaringClass,
                methodName,
                signature,
                reader.getInt(),
                reader.getBytes(),
            )
        }
    }
}

/**
 *     ClassPoolObject {
 *         sint8 type = POOL_NODE_CLASS
 *         PoolObject node_class
 *         String name_template
 *         sint16 input_count
 *         InputEdgeInfo[input_count] inputs
 *         sint16 output_count
 *         OutputEdgeInfo[output_count] outputs
 *     }
 */
class BGVNodeClassPool(
    id: UShort,
    val nodeClass: BGVClassPool,
    val nameTemplate: String,
    val inputs: List<BGVInputEdgeInfo>,
    val outputs: List<BGVOutputEdgeInfo>,
) : BGVNewPool(id, BGVToken.POOL_NODE_CLASS) {
    override fun writeImpl(writer: ExpandingByteBuffer, context: Context) {
        super.writeImpl(writer, context)
        nodeClass.write(writer, context)
        writer.putString(nameTemplate)
        writer.putShort(inputs.size.toShort())
        inputs.forEach { it.write(writer, context) }
        writer.putShort(outputs.size.toShort())
        outputs.forEach { it.write(writer, context) }
    }

    override fun toJsonImpl(context: Context) = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "node_class")
        put("id", id.toInt())
        put("node_class", nodeClass.toJson(context))
        put("name_template", nameTemplate)
        putJsonArray("inputs") {
            inputs.forEach { add(it.toJson(context)) }
        }
        putJsonArray("outputs") {
            outputs.forEach { add(it.toJson(context)) }
        }
    }

    override fun toString() = "NodeClassPool {class=$nodeClass, template=$nameTemplate}"

    companion object : IBGVReader<BGVNodeClassPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNodeClassPool {
            val nodeClass = IBGVPoolObject.read(reader, context)
            expectIs<BGVClassPool>(nodeClass)

            val nameTemplate = reader.getString()

            val inputSize = reader.getShort()
            val inputs = (0 until inputSize).map { BGVInputEdgeInfo.read(reader, context) }
            val outputSize = reader.getShort()
            val outputs = (0 until outputSize).map { BGVOutputEdgeInfo.read(reader, context) }

            return BGVNodeClassPool(0U, nodeClass, nameTemplate, inputs, outputs)
        }
    }
}

/**
 *     FieldPoolObject {
 *         sint8 type = POOL_FIELD
 *         PoolObject declaring_class
 *         PoolObject name
 *         PoolObject type_name
 *         sint32 modifiers
 *     }
 */
class BGVFieldPool(
    id: UShort,
    val declaringClass: BGVClassPool,
    val name: BGVStringPool,
    val typeName: BGVStringPool,
    val modifiers: Int,
) : BGVNewPool(id, BGVToken.POOL_FIELD) {
    override fun writeImpl(writer: ExpandingByteBuffer, context: Context) {
        super.writeImpl(writer, context)
        declaringClass.write(writer, context)
        name.write(writer, context)
        typeName.write(writer, context)
        writer.putInt(modifiers)
    }

    override fun toJsonImpl(context: Context) = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "field")
        put("id", id.toInt())
        put("declaring_class", declaringClass.toJson(context))
        put("name", name.toJson(context))
        put("type_name", typeName.toJson(context))
        put("modifiers", modifiers)
    }

    override fun toString() = "FieldPool {name=$name, class=$declaringClass, type=$typeName}"

    companion object : IBGVReader<BGVFieldPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVFieldPool {
            val declaringClass = IBGVPoolObject.read(reader, context)
            val name = IBGVPoolObject.read(reader, context)
            val typeName = IBGVPoolObject.read(reader, context)

            expectIs<BGVClassPool>(declaringClass)
            expectIs<BGVStringPool>(name)
            expectIs<BGVStringPool>(typeName)

            return BGVFieldPool(0U, declaringClass, name, typeName, reader.getInt())
        }
    }
}

/**
 *     NodeSignaturePool {
 *         sint8 type = POOL_NODE_SIGNATURE
 *         sint16 args_count
 *         PoolObject args[args_count]
 *         PoolObject return
 *     }
 */
class BGVNodeSignaturePool(
    id: UShort,
    val args: List<BGVStringPool>,
    val returnType: BGVStringPool,
) : BGVNewPool(id, BGVToken.POOL_SIGNATURE) {
    override fun writeImpl(writer: ExpandingByteBuffer, context: Context) {
        super.writeImpl(writer, context)
        writer.putShort(args.size.toShort())
        args.forEach { it.write(writer, context) }
        returnType.write(writer, context)
    }

    override fun toJsonImpl(context: Context) = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "node_signature")
        put("id", id.toInt())
        putJsonArray("args") {
            args.forEach { add(it.toJson(context)) }
        }
        put("return_type", returnType.toJson(context))
    }

    override fun toString() = "NodeSignaturePool {args=[${args.joinToString()}], return=$returnType}"

    companion object : IBGVReader<BGVNodeSignaturePool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNodeSignaturePool {
            val length = reader.getShort()
            val args = (0 until length).map {
                val arg = IBGVPoolObject.read(reader, context)
                expectIs<BGVStringPool>(arg)
                arg
            }

            val returnType = IBGVPoolObject.read(reader, context)
            expectIs<BGVStringPool>(returnType)

            return BGVNodeSignaturePool(0U, args, returnType)
        }
    }
}

/**
 *     SourcePosition {
 *         PoolObject uri
 *         String location
 *         sint32 line
 *         sint32 start
 *         sint32 end
 *     }
 */
data class SourcePosition(
    val uri: BGVStringPool,
    val location: String,
    val line: Int,
    val start: Int,
    val end: Int,
)

/**
 *     NodeSourcePositionPool {
 *         sint8 type = POOL_NODE_SOURCE_POSITION
 *         PoolObject method
 *         sint32 bci
 *         SourcePosition[...until SourcePosition.uri = null] source_positions
 *         PoolObject caller
 *     }
 *
 */
class BGVNodeSourcePositionPool(
    id: UShort,
    val method: BGVMethodPool,
    val bci: Int, // bytecode index
    val sourcePositions: List<SourcePosition>,
    val caller: BGVNodeSourcePositionPool?,
) : BGVNewPool(id, BGVToken.POOL_NODE_SOURCE_POSITION) {
    override fun writeImpl(writer: ExpandingByteBuffer, context: Context) {
        super.writeImpl(writer, context)
        method.write(writer, context)
        writer.putInt(bci)
        sourcePositions.forEach {
            it.uri.write(writer, context)
            writer.putString(it.location)
            writer.putInt(it.line)
            writer.putInt(it.start)
            writer.putInt(it.end)
        }
        BGVNullPool.write(writer, context)
        caller.write(writer, context)
    }

    override fun toJsonImpl(context: Context): JsonElement = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "node_source_position")
        put("id", id.toInt())
        put("method", method.toJson(context))
        put("bci", bci)
        putJsonArray("source_positions") {
            sourcePositions.forEach {
                addJsonObject {
                    put("uri", it.uri.toJson(context))
                    put("location", it.location)
                    put("line", it.line)
                    put("start", it.start)
                    put("end", it.end)
                }
            }
        }
        val callerEl: JsonElement = caller?.toJson(context) ?: JsonNull
        put("caller", callerEl)
    }

    companion object : IBGVReader<BGVNodeSourcePositionPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNodeSourcePositionPool {
            val method = IBGVPoolObject.read(reader, context)
            expectIs<BGVMethodPool>(method)

            val bci = reader.getInt()
            val sourcePositions = mutableListOf<SourcePosition>()

            while (reader.peekByte() != BGVToken.POOL_NULL) {
                val uri = IBGVPoolObject.read(reader, context)
                expectIs<BGVStringPool>(uri)

                sourcePositions.add(SourcePosition(
                    uri,
                    reader.getString(),
                    reader.getInt(),
                    reader.getInt(),
                    reader.getInt(),
                ))
            }
            reader.getByte()

            val caller = IBGVPoolObject.read(reader, context).toNullType()
            expectIs<BGVNodeSourcePositionPool?>(caller)

            return BGVNodeSourcePositionPool(0U, method, bci, sourcePositions, caller)
        }
    }
}

/**
 *     NodePoolObject {
 *         sint8 type = POOL_NODE
 *         sint32 node_id
 *         PoolObject node_class
 *     }
 */
class BGVNodePool(
    id: UShort,
    val nodeId: Int,
    val nodeClass: BGVNodeClassPool,
) : BGVNewPool(id, BGVToken.POOL_NODE) {
    override fun writeImpl(writer: ExpandingByteBuffer, context: Context) {
        super.writeImpl(writer, context)
        writer.putInt(nodeId)
        nodeClass.write(writer, context)
    }

    override fun toJsonImpl(context: Context) = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "node")
        put("id", id.toInt())
        put("node_id", nodeId)
        put("node_class", nodeClass.toJson(context))
    }

    override fun toString() = "NodePool {id=$nodeId, class=$nodeClass}"

    companion object : IBGVReader<BGVNodePool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNodePool {
            val id = reader.getInt()
            val nodeClass = IBGVPoolObject.read(reader, context)
            expectIs<BGVNodeClassPool>(nodeClass)
            return BGVNodePool(0U, id, nodeClass)
        }
    }
}

fun <T : IBGVPoolObject> T.toNullType(): T? = if (this == BGVNullPool) null else this

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun <T : IBGVPoolObject?> T.write(writer: ExpandingByteBuffer, context: Context) =
    (this ?: BGVNullPool).write(writer, context)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun <T : IBGVPoolObject?> T.toJson(context: Context): JsonElement = (this ?: BGVNullPool).toJson(context)
