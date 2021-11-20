package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class BGVProp(val key: IBGVPoolObject, val value: IBGVPropObject) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer) {
        key.write(writer)
        value.write(writer)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "prop")
        put("key", key.toJson())
        put("value", value.toJson())
    }

    override fun toString() = "Prop {key=$key, value=$value}"

    companion object : IBGVReader<BGVProp> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVProp {
            val key = IBGVPoolObject.read(reader, context)
            val value = IBGVPropObject.read(reader, context)
            return BGVProp(key, value)
        }
    }
}