package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVProps(val props: List<BGVProp>) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putShort(props.size.toShort())
        props.forEach { it.write(writer) }
    }

    companion object : IBGVReader<BGVProps> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVProps {
            val propsSize = reader.getShort()
            return BGVProps((0 until propsSize).map { BGVProp.read(reader, context) })
        }
    }
}