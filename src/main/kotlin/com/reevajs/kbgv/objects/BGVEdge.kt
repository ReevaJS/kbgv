package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

interface IBGVEdge : IBGVWriter

class BGVDirectEdge(val node: Int) : IBGVEdge {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putInt(node)
    }

    companion object : IBGVReader<BGVDirectEdge> {
        override fun read(reader: ExpandingByteBuffer): BGVDirectEdge {
            return BGVDirectEdge(reader.getInt())
        }
    }
}

class BGVIndirectEdge(val nodes: List<Int>) : IBGVEdge {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putShort(nodes.size.toShort())
        nodes.forEach(writer::putInt)
    }

    companion object : IBGVReader<BGVIndirectEdge> {
        override fun read(reader: ExpandingByteBuffer): BGVIndirectEdge {
            val size = reader.getShort()
            return BGVIndirectEdge((0 until size).map { reader.getInt() })
        }
    }
}
