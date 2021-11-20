package com.reevajs.kbgv.objects

import com.reevajs.kbgv.*
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 *     PropObject {
 *         union {
 *             PoolPropObject
 *             IntPropObject
 *             LongPropObject
 *             DoublePropObject
 *             FloatPropObject
 *             TruePropObject
 *             FalsePropObject
 *             ArrayPropObject
 *             SubgraphPropObject
 *         }
 *     }
 */
sealed interface IBGVPropObject : IBGVObject {
    companion object : IBGVReader<IBGVPropObject> {
        override fun read(reader: ExpandingByteBuffer, context: Context): IBGVPropObject {
            return when (reader.getByte()) {
                BGVToken.PROPERTY_POOL -> BGVPoolProperty(IBGVPoolObject.read(reader, context).toNullType())
                BGVToken.PROPERTY_INT -> BGVIntProperty(reader.getInt())
                BGVToken.PROPERTY_LONG -> BGVLongProperty(reader.getLong())
                BGVToken.PROPERTY_DOUBLE -> BGVDoubleProperty(reader.getDouble())
                BGVToken.PROPERTY_FLOAT -> BGVFloatProperty(reader.getFloat())
                BGVToken.PROPERTY_TRUE -> BGVTrueProperty
                BGVToken.PROPERTY_FALSE -> BGVFalseProperty
                BGVToken.PROPERTY_ARRAY -> BGVArrayProperty.read(reader, context)
                BGVToken.PROPERTY_SUBGRAPH -> BGVSubgraphProperty(BGVGraphBody.read(reader, context))
                else -> unreachable()
            }
        }
    }
}

/**
 *     PoolPropObject {
 *         sint8 type = PROPERTY_POOL
 *         PoolObject object
 *     }
 */
data class BGVPoolProperty(val value: IBGVPoolObject?) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_POOL)
        value.write(writer)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "prop_object")
        put("prop_type", "pool")
        put("value", value.toJson())
    }

    override fun toString() = "$value"
}

/**
 *     IntPropObject {
 *         sint8 type = PROPERTY_INT
 *         sint32 value
 *     }
 */
data class BGVIntProperty(val value: Int) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_INT)
        writer.putInt(value)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "prop_object")
        put("prop_type", "pool")
        put("value", value)
    }

    override fun toString() = "$value"
}

/**
 *     LongPropObject {
 *         sint8 type = PROPERTY_LONG
 *         sint64 value
 *     }
 */
data class BGVLongProperty(val value: Long) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_LONG)
        writer.putLong(value)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "prop_object")
        put("prop_type", "long")
        put("value", value)
    }

    override fun toString() = "${value}L"
}

/**
 *     DoublePropObject {
 *         sint8 type = PROPERTY_DOUBLE
 *         float64 value
 *     }
 */
data class BGVDoubleProperty(val value: Double) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_DOUBLE)
        writer.putDouble(value)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "prop_object")
        put("prop_type", "double")
        put("value", value)
    }

    override fun toString() = "$value"
}

/**
 *     FloatPropObject {
 *         sint8 type = PROPERTY_FLOAT
 *         float32 value
 *     }
 */
data class BGVFloatProperty(val value: Float) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_FLOAT)
        writer.putFloat(value)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "prop_object")
        put("prop_type", "float")
        put("value", value)
    }

    override fun toString() = "${value}F"
}

/**
 *     TruePropObject {
 *         sint8 type = PROPERTY_TRUE
 *     }
 */
object BGVTrueProperty : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_TRUE)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "prop_object")
        put("prop_type", "true")
    }

    override fun toString() = "true"
}

/**
 *     FalsePropObject {
 *         sint8 type = PROPERTY_FALSE
 *     }
 */
object BGVFalseProperty : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_FALSE)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "prop_object")
        put("prop_type", "false")
    }

    override fun toString() = "false"
}

/**
 *     ArrayPropObject {
 *         sint8 type = PROPERTY_ARRAY
 *         union {
 *             struct {
 *                 sint8 array_type = PROPERTY_DOUBLE
 *                 sint32 times
 *                 float64[times] values
 *             }
 *             struct {
 *                 sint8 array_type = PROPERTY_INT
 *                 sint32 times
 *                 sint32[times] values
 *             }
 *             struct {
 *                 sint8 array_type = PROPERTY_POOL
 *                 sint32 times
 *                 PoolObject[times] values
 *             }
 *         }
 *     }
 */
class BGVArrayProperty(
    val values: List<Any>, // List<Double | Int | IBGVPoolObject>
) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_ARRAY)
        val type = when (values::class.java.componentType) {
            Double::class -> BGVToken.PROPERTY_DOUBLE
            Int::class -> BGVToken.PROPERTY_INT
            else -> BGVToken.PROPERTY_POOL
        }

        writer.putByte(BGVToken.PROPERTY_POOL)
        writer.putInt(values.size)

        when (type) {
            BGVToken.PROPERTY_DOUBLE -> values.forEach {
                expectIs<Double>(it)
                writer.putDouble(it)
            }
            BGVToken.PROPERTY_INT -> values.forEach {
                expectIs<Int>(it)
                writer.putInt(it)
            }
            else -> values.forEach {
                expectIs<IBGVPoolObject>(it)
                it.write(writer)
            }
        }
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "prop_object")
        val type = when (values::class.java.componentType) {
            Double::class -> "double"
            Int::class -> "int"
            else -> "pool"
        }
        put("prop_type", "array")
        put("array_type", type)
        putJsonArray("values") {
            values.forEach {
                when (type) {
                    "double" -> add(it as Double)
                    "int" -> add(it as Int)
                    "pool" -> add((it as IBGVPoolObject).toJson())
                    else -> unreachable()
                }
            }
        }
    }

    override fun toString() = "[${values.joinToString()}]"

    companion object : IBGVReader<BGVArrayProperty> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVArrayProperty {
            val type = reader.getByte()

            val count = reader.getInt()
            val values = (0 until count).map {
                when (type) {
                    BGVToken.PROPERTY_DOUBLE -> reader.getDouble()
                    BGVToken.PROPERTY_INT -> reader.getInt()
                    BGVToken.PROPERTY_POOL -> IBGVPoolObject.read(reader, context)
                    else -> unreachable()
                }
            }

            return BGVArrayProperty(values)
        }
    }
}

/**
 *     SubgraphPropObject {
 *         sint8 type = PROPERTY_SUBGRAPH
 *         GraphBody graph
 *     }
 */
class BGVSubgraphProperty(val graph: BGVGraphBody) : IBGVPropObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.PROPERTY_SUBGRAPH)
        graph.write(writer)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "prop_object")
        put("prop_type", "subgraph")
        put("graph", graph.toJson())
    }
}
