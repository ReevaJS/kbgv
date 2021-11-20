package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer
import com.reevajs.kbgv.expectIs
import kotlinx.serialization.json.*
import java.util.*

/**
 * PoolObject {
 *   union {
 *     POOL_NULL
 *     struct {
 *       sint8 token = POOL_NEW
 *       uint16 id
 *       union {
 *         struct {
 *           sint8 type = POOL_STRING
 *           String string
 *         }
 *         struct {
 *           sint8 type = POOL_ENUM
 *           PoolObject enum_class
 *           sint32 enum_ordinal
 *         }
 *         struct {
 *           sint8 type = POOL_CLASS
 *           String type_name
 *           union {
 *             struct {
 *               sint8 type = ENUM_KLASS
 *               sint32 values_count
 *               PoolObject values[values_count]
 *             }
 *             struct {
 *               sint8 type = KLASS
 *             }
 *           }
 *         }
 *         struct {
 *           sint8 type = POOL_METHOD
 *           PoolObject declaring_class
 *           PoolObject method_name
 *           PoolObject signature
 *           sint32 modifiers
 *           sint32 bytes_length
 *           uint8[bytes_length] bytes
 *         }
 *         struct {
 *           sint8 type = POOL_NODE_CLASS
 *           PoolObject node_class
 *           String name_template
 *           sint16 input_count
 *           InputEdgeInfo inputs[input_count]
 *           sint16 output_count
 *           OutputEdgeInfo outputs[output_count]
 *         }
 *         struct {
 *           sint8 type = POOL_FIELD
 *           PoolObject field_class
 *           PoolObject name
 *           PoolObject type_name
 *           sint32 modifiers
 *         }
 *         struct {
 *           sint8 type = POOL_NODE_SIGNATURE
 *           sint16 args_count
 *           PoolObject args[args_count]
 *         }
 *         struct {
 *           sint8 type = POOL_NODE_SOURCE_POSITION
 *           PoolObject method
 *           sint32 bci
 *           SourcePosition source_positions[...until SourcePosition.uri = null]
 *           PoolObject caller
 *         }
 *         struct {
 *           sint8 type = POOL_NODE
 *           sint32 node_id
 *           PoolObject node_class
 *         }
 *       }
 *     }
 *     struct {
 *       sint8 token = POOL_STRING | POOL_ENUM | POOL_CLASS | POOL_METHOD | POOL_NODE_CLASS | POOL_FIELD | POOL_SIGNATURE | POOL_NODE_SOURCE_POSITION | POOL_NODE
 *       uint16 pool_id
 *     }
 *   }
 * }
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
                BGVToken.POOL_NEW -> BGVNonnullPool.read(reader, context)
                else -> {
                    if (type !in ALLOWED_REF_TYPES)
                        throw IllegalStateException()
                    context[reader.getShort().toUShort()]
                }
            }
        }
    }
}

object BGVNullPool : IBGVPoolObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.POOL_NULL)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "null")
    }

    override fun toString() = "NullPool"
}

sealed class BGVNonnullPool(id: UShort) : IBGVPoolObject {
    var id: UShort = id
        internal set

    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.POOL_NEW)
        writer.putShort(id.toShort())
    }

    companion object : IBGVReader<BGVNonnullPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNonnullPool {
            val id = reader.getShort().toUShort()

            return when (reader.getByte()) {
                BGVToken.POOL_STRING -> BGVStringPool.read(reader, context)
                BGVToken.POOL_ENUM -> BGVEnumPool.read(reader, context)
                BGVToken.POOL_CLASS -> BGVClassPool.read(reader, context)
                BGVToken.POOL_METHOD -> BGVMethodPool.read(reader, context)
                BGVToken.POOL_NODE_CLASS -> BGVNodeClassPool.read(reader, context)
                BGVToken.POOL_FIELD -> BGVFieldPool.read(reader, context)
                BGVToken.POOL_SIGNATURE -> BGVNodeSignaturePool.read(reader, context)
                BGVToken.POOL_NODE_SOURCE_POSITION -> BGVNodeSourcePositionPool.read(reader, context)
                BGVToken.POOL_NODE -> BGVNodePool.read(reader, context)
                else -> throw IllegalStateException()
            }.also {
                it.id = id
                context[id] = it
            }
        }
    }
}

class BGVStringPool(id: UShort, val string: String) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_STRING)
        writer.putString(string)
    }

    override fun toJson() = buildJsonObject {
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

class BGVEnumPool(
    id: UShort,
    val enumClass: BGVClassPool,
    val ordinal: Int,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_ENUM)
        enumClass.write(writer)
        writer.putInt(ordinal)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "enum")
        put("id", id.toInt())
        put("enum_class", enumClass.toJson())
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

sealed interface IBGVClassPoolType : IBGVObject

class BGVClassPoolEnumType(val values: List<BGVStringPool>) : IBGVClassPoolType {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.ENUM_KLASS)
        writer.putInt(values.size)
        values.forEach {
            it.write(writer)
        }
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "pool")
        put("class_type", "enum_klass")
        putJsonArray("values") {
            values.forEach { add(it.toJson()) }
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

object BGVClassPoolKlassType : IBGVClassPoolType {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.KLASS)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "pool")
        put("class_type", "klass")
    }

    override fun toString() = "KLASS"
}

class BGVClassPool(
    id: UShort,
    val typeName: String,
    val type: IBGVClassPoolType,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_CLASS)
        writer.putString(typeName)
        type.write(writer)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "class")
        put("id", id.toInt())
        put("type_name", typeName)
        put("type", type.toJson())
    }

    override fun toString() = "ClassPool {name=$typeName, type=$type}"

    companion object : IBGVReader<BGVClassPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVClassPool {
            val typeName = reader.getString()
            val type = when (reader.getByte()) {
                BGVToken.KLASS -> BGVClassPoolKlassType
                BGVToken.ENUM_KLASS -> BGVClassPoolEnumType.read(reader, context)
                else -> throw IllegalStateException()
            }
            return BGVClassPool(0U, typeName, type)
        }
    }
}

class BGVMethodPool(
    id: UShort,
    val declaringClass: BGVClassPool,
    val methodName: BGVStringPool,
    val signature: BGVNodeSignaturePool,
    val modifiers: Int,
    val bytes: ByteArray,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_METHOD)
        declaringClass.write(writer)
        methodName.write(writer)
        signature.write(writer)
        writer.putInt(modifiers)
        writer.putBytes(bytes)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "method")
        put("id", id.toInt())
        put("declaring_class", declaringClass.toJson())
        put("method_name", methodName.toJson())
        put("signature", signature.toJson())
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

class BGVNodeClassPool(
    id: UShort,
    val nodeClass: BGVClassPool,
    val nameTemplate: String,
    val inputs: List<BGVInputEdgeInfo>,
    val outputs: List<BGVOutputEdgeInfo>,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_NODE_CLASS)
        nodeClass.write(writer)
        writer.putString(nameTemplate)
        writer.putShort(inputs.size.toShort())
        inputs.forEach { it.write(writer) }
        writer.putShort(outputs.size.toShort())
        outputs.forEach { it.write(writer) }
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "node_class")
        put("id", id.toInt())
        put("node_class", nodeClass.toJson())
        put("name_template", nameTemplate)
        putJsonArray("inputs") {
            inputs.forEach { add(it.toJson()) }
        }
        putJsonArray("outputs") {
            outputs.forEach { add(it.toJson()) }
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

class BGVFieldPool(
    id: UShort,
    val declaringClass: BGVClassPool,
    val name: BGVStringPool,
    val typeName: BGVStringPool,
    val modifiers: Int,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_FIELD)
        declaringClass.write(writer)
        name.write(writer)
        typeName.write(writer)
        writer.putInt(modifiers)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "field")
        put("id", id.toInt())
        put("declaring_class", declaringClass.toJson())
        put("name", name.toJson())
        put("type_name", typeName.toJson())
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

class BGVNodeSignaturePool(
    id: UShort,
    val args: List<BGVStringPool>,
    val returnType: BGVStringPool,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putShort(args.size.toShort())
        args.forEach { it.write(writer) }
        returnType.write(writer)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "node_signature")
        put("id", id.toInt())
        putJsonArray("args") {
            args.forEach { add(it.toJson()) }
        }
        put("return_type", returnType.toJson())
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

data class SourcePosition(
    val uri: BGVStringPool,
    val location: String,
    val line: Int,
    val start: Int,
    val end: Int,
)

class BGVNodeSourcePositionPool(
    id: UShort,
    val method: BGVMethodPool,
    val bci: Int, // bytecode index
    val sourcePositions: List<SourcePosition>,
    val caller: BGVNodeSourcePositionPool?,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_NODE_SOURCE_POSITION)
        method.write(writer)
        writer.putInt(bci)
        sourcePositions.forEach {
            it.uri.write(writer)
            writer.putString(it.location)
            writer.putInt(it.line)
            writer.putInt(it.start)
            writer.putInt(it.end)
        }
        BGVNullPool.write(writer)
        caller.write(writer)
    }

    override fun toJson(): JsonElement = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "node_source_position")
        put("id", id.toInt())
        put("method", method.toJson())
        put("bci", bci)
        putJsonArray("source_positions") {
            sourcePositions.forEach {
                addJsonObject {
                    put("uri", it.uri.toJson())
                    put("location", it.location)
                    put("line", it.line)
                    put("start", it.start)
                    put("end", it.end)
                }
            }
        }
        val callerEl: JsonElement = caller?.toJson() ?: JsonNull
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

class BGVNodePool(
    id: UShort,
    val nodeId: Int,
    val nodeClass: BGVNodeClassPool,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_NODE)
        writer.putInt(nodeId)
        nodeClass.write(writer)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "pool")
        put("pool_type", "node")
        put("id", id.toInt())
        put("node_id", nodeId)
        put("node_class", nodeClass.toJson())
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
fun <T : IBGVPoolObject?> T.write(writer: ExpandingByteBuffer) = (this ?: BGVNullPool).write(writer)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun <T : IBGVPoolObject?> T.toJson(): JsonElement = (this ?: BGVNullPool).toJson()
