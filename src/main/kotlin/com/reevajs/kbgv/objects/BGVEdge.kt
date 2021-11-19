package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

class BGVEdge(val nodes: Collection<Int>) : IBGVWriter {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putShort(nodes.size.toShort())
        nodes.forEach(writer::putInt)
    }

    override fun toString(): String {
        return "Edge (${nodes.joinToString()})"
    }

    companion object : IBGVReader<BGVEdge> {
        override fun read(reader: ExpandingByteBuffer): BGVEdge {
            val size = reader.getShort()
            return BGVEdge((0 until size).map { reader.getInt() })
        }
    }
}
