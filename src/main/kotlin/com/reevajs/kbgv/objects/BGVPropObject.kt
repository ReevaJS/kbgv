package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer

sealed interface IBGVPropObject : IBGVWriter {
    companion object : IBGVReader<IBGVPropObject> {
        override fun read(reader: ExpandingByteBuffer): IBGVPropObject {
            return when (reader.getByte()) {
                BGVToken.PROPERTY_POOL -> BGVPoolProperty(IBGVPoolObject.read(reader))
                BGVToken.PROPERTY_INT -> BGVIntProperty(reader.getInt())
                BGVToken.PROPERTY_LONG -> BGVLongProperty(reader.getLong())
                BGVToken.PROPERTY_DOUBLE -> BGVDoubleProperty(reader.getDouble())
                BGVToken.PROPERTY_FLOAT -> BGVFloatProperty(reader.getFloat())
                BGVToken.PROPERTY_TRUE -> BGVTrueProperty
                BGVToken.PROPERTY_FALSE -> BGVFalseProperty
                BGVToken.PROPERTY_ARRAY -> BGVArrayProperty.read(reader)
                BGVToken.PROPERTY_SUBGRAPH -> BGVSubgraphProperty(BGVGraphBody.read(reader))
                else -> throw IllegalStateException()
            }
        }
    }
}

data class BGVPoolProperty(val obj: IBGVPoolObject) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_POOL)
        obj.write(writer)
    }

    override fun toString() = "PoolProperty ($obj)"
}

data class BGVIntProperty(val value: Int) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_INT)
        writer.putInt(value)
    }

    override fun toString() = "IntProperty ($value)"
}

data class BGVLongProperty(val value: Long) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_LONG)
        writer.putLong(value)
    }

    override fun toString() = "LongProperty ($value)"
}

data class BGVDoubleProperty(val value: Double) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_DOUBLE)
        writer.putDouble(value)
    }

    override fun toString() = "DoubleProperty ($value)"
}

data class BGVFloatProperty(val value: Float) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_FLOAT)
        writer.putFloat(value)
    }

    override fun toString() = "FloatProperty ($value)"
}

object BGVTrueProperty : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_TRUE)
    }

    override fun toString() = "TrueProperty"
}

object BGVFalseProperty : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_FALSE)
    }

    override fun toString() = "FalseProperty"
}

class BGVArrayProperty(
    val values: Collection<Any>, // Collection<Double | Int | IBGVPoolObject>
) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_ARRAY)
        val type = when (values::class.java.componentType) {
            Double::class -> BGVToken.PROPERTY_DOUBLE
            Int::class -> BGVToken.PROPERTY_DOUBLE
            else -> BGVToken.PROPERTY_POOL
        }

        writer.putByte(BGVToken.PROPERTY_POOL)
        writer.putInt(values.size)

        when (type) {
            BGVToken.PROPERTY_DOUBLE -> values.forEach {
                if (it !is Double)
                    throw IllegalStateException()
                writer.putDouble(it)
            }
            BGVToken.PROPERTY_INT -> values.forEach {
                if (it !is Int)
                    throw IllegalStateException()
                writer.putInt(it)
            }
            else -> values.forEach {
                if (it !is IBGVPoolObject)
                    throw IllegalStateException()
                it.write(writer)
            }
        }
    }

    companion object : IBGVReader<BGVArrayProperty> {
        override fun read(reader: ExpandingByteBuffer): BGVArrayProperty {
            TODO("Not yet implemented")
        }
    }
}

class BGVSubgraphProperty(val graph: BGVGraphBody) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_SUBGRAPH)
        graph.write(writer)
    }
}
