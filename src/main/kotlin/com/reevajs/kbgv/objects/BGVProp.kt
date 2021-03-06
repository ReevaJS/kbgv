package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import com.reevajs.kbgv.expectIs
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 *     Prop {
 *         PoolObject key
 *         PropObject value
 *     }
 */
class BGVProp(val key: BGVStringPool, val value: IBGVPropObject) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer, context: Context) {
        key.write(writer, context)
        value.write(writer, context)
    }

    override fun toJson(context: Context) = buildJsonObject {
        put("\$type", "prop")
        put("key", key.toJson(context))
        put("value", value.toJson(context))
    }

    override fun toString() = "Prop {key=$key, value=$value}"

    companion object : IBGVReader<BGVProp> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVProp {
            val key = IBGVPoolObject.read(reader, context)
            val value = IBGVPropObject.read(reader, context)
            expectIs<BGVStringPool>(key)
            return BGVProp(key, value)
        }
    }
}