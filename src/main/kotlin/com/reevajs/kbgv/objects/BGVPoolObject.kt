package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer

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
        override fun read(reader: ExpandingByteBuffer, context: Context): IBGVPoolObject {
            return when (reader.peekByte()) {
                BGVToken.POOL_NULL -> {
                    reader.getByte()
                    BGVNullPool
                }
                BGVToken.POOL_NEW -> {
                    reader.getByte()
                    BGVNonnullPool.read(reader, context)
                }
                else -> BGVPoolObjectRef.read(reader, context)
            }
        }
    }
}

object BGVNullPool : IBGVPoolObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.POOL_NULL)
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

    override fun toString() = "StringPool #$id ($string)"

    companion object : IBGVReader<BGVStringPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVStringPool {
            return BGVStringPool(0U, reader.getString())
        }
    }
}

class BGVEnumPool(
    id: UShort,
    val enumClass: IBGVPoolObject,
    val ordinal: Int,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_ENUM)
        enumClass.write(writer)
        writer.putInt(ordinal)
    }

    companion object : IBGVReader<BGVEnumPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVEnumPool {
            return BGVEnumPool(0U, IBGVPoolObject.read(reader, context), reader.getInt())
        }
    }
}

sealed interface IBGVClassPoolType : IBGVObject

class BGVClassPoolEnumType(val values: List<IBGVPoolObject>) : IBGVClassPoolType {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.ENUM_KLASS)
        writer.putInt(values.size)
        values.forEach {
            it.write(writer)
        }
    }

    companion object : IBGVReader<BGVClassPoolEnumType> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVClassPoolEnumType {
            val length = reader.getInt()
            return BGVClassPoolEnumType((0 until length).map { IBGVPoolObject.read(reader, context) })
        }
    }
}

object BGVClassPoolKlassType : IBGVClassPoolType {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.KLASS)
    }
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
    val declaringClass: IBGVPoolObject,
    val methodName: IBGVPoolObject,
    val signature: IBGVPoolObject,
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

    companion object : IBGVReader<BGVMethodPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVMethodPool {
            return BGVMethodPool(
                0U,
                IBGVPoolObject.read(reader, context),
                IBGVPoolObject.read(reader, context),
                IBGVPoolObject.read(reader, context),
                reader.getInt(),
                reader.getBytes(),
            )
        }
    }
}

class BGVNodeClassPool(
    id: UShort,
    val nodeClass: IBGVPoolObject,
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

    companion object : IBGVReader<BGVNodeClassPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNodeClassPool {
            val nodeClass = IBGVPoolObject.read(reader, context)
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
    val fieldClass: IBGVPoolObject,
    val name: IBGVPoolObject,
    val typeName: IBGVPoolObject,
    val modifiers: Int,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_FIELD)
        fieldClass.write(writer)
        name.write(writer)
        typeName.write(writer)
        writer.putInt(modifiers)
    }

    companion object : IBGVReader<BGVFieldPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVFieldPool {
            return BGVFieldPool(
                0U,
                IBGVPoolObject.read(reader, context),
                IBGVPoolObject.read(reader, context),
                IBGVPoolObject.read(reader, context),
                reader.getInt(),
            )
        }
    }
}

class BGVNodeSignaturePool(
    id: UShort,
    val args: List<IBGVPoolObject>,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putShort(args.size.toShort())
        args.forEach { it.write(writer) }
    }

    companion object : IBGVReader<BGVNodeSignaturePool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNodeSignaturePool {
            val length = reader.getShort()
            return BGVNodeSignaturePool(0U, (0 until length).map { IBGVPoolObject.read(reader, context) })
        }
    }
}

class BGVNodeSourcePositionPool(
    id: UShort,
    val method: IBGVPoolObject,
    val bci: Int, // bytecode index
    val sourcePositions: List<BGVSourcePosition>,
    val caller: IBGVPoolObject,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_NODE_SOURCE_POSITION)
        method.write(writer)
        writer.putInt(bci)
        sourcePositions.forEach { it.write(writer) }
        BGVNullPool.write(writer)
        caller.write(writer)
    }

    companion object : IBGVReader<BGVNodeSourcePositionPool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNodeSourcePositionPool {
            val method = IBGVPoolObject.read(reader, context)
            val bci = reader.getInt()
            val sourcePositions = mutableListOf<BGVSourcePosition>()
            while (reader.peekByte() != BGVToken.POOL_NULL)
                sourcePositions.add(BGVSourcePosition.read(reader, context))
            val caller = IBGVPoolObject.read(reader, context)

            return BGVNodeSourcePositionPool(0U, method, bci, sourcePositions, caller)
        }
    }
}

class BGVNodePool(
    id: UShort,
    val nodeId: Int,
    val nodeClass: IBGVPoolObject,
) : BGVNonnullPool(id) {
    override fun write(writer: ExpandingByteBuffer) {
        super.write(writer)
        writer.putByte(BGVToken.POOL_NODE)
        writer.putInt(nodeId)
        nodeClass.write(writer)
    }

    companion object : IBGVReader<BGVNodePool> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNodePool {
            return BGVNodePool(0U, reader.getInt(), IBGVPoolObject.read(reader, context))
        }
    }
}

class BGVPoolObjectRef(val type: Byte, val id: UShort) : IBGVPoolObject {
    init {
        if (type !in ALLOWED_TOKENS)
            throw IllegalArgumentException("$type is not an allowable pool reference token type")
    }

    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(type)
        writer.putShort(id.toShort())
    }

    override fun toString() = "Ref #$id"

    companion object : IBGVReader<BGVPoolObjectRef> {
        private val ALLOWED_TOKENS = setOf(
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

        override fun read(reader: ExpandingByteBuffer, context: Context): BGVPoolObjectRef {
            val token = reader.getByte()
            if (token !in ALLOWED_TOKENS)
                throw IllegalStateException()
            return BGVPoolObjectRef(token, reader.getShort().toUShort())
        }
    }
}
