package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

data class BGVProps(val props: List<BGVProp>) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putShort(props.size.toShort())
        props.forEach { it.write(writer) }
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "props")
        putJsonArray("values") {
            props.forEach { add(it.toJson()) }
        }
    }

    companion object : IBGVReader<BGVProps> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVProps {
            val propsSize = reader.getShort()
            return BGVProps((0 until propsSize).map { BGVProp.read(reader, context) })
        }
    }
}