package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 *     Props {
 *         sint16 props_count
 *         Prop[props_count] props
 *     }
 */
data class BGVProps(val props: List<BGVProp>) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer, context: Context) {
        writer.putShort(props.size.toShort())
        props.forEach { it.write(writer, context) }
    }

    override fun toJson(context: Context) = buildJsonObject {
        put("\$type", "props")
        putJsonArray("values") {
            props.forEach { add(it.toJson(context)) }
        }
    }

    companion object : IBGVReader<BGVProps> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVProps {
            val propsSize = reader.getShort()
            return BGVProps((0 until propsSize).map { BGVProp.read(reader, context) })
        }
    }
}