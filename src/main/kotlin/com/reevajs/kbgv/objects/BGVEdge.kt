package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

interface IBGVEdge : IBGVObject

class BGVDirectEdge(val node: Int) : IBGVEdge {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putInt(node)
    }

    override fun toString() = "DirectEdge {$node}"

    companion object : IBGVReader<BGVDirectEdge> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVDirectEdge {
            return BGVDirectEdge(reader.getInt())
        }
    }
}

class BGVIndirectEdge(val nodes: List<Int>) : IBGVEdge {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putShort(nodes.size.toShort())
        nodes.forEach(writer::putInt)
    }

    override fun toString() = "IndirectEdge {[${nodes.joinToString()}]}"

    companion object : IBGVReader<BGVIndirectEdge> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVIndirectEdge {
            val size = reader.getShort()
            return BGVIndirectEdge((0 until size).map { reader.getInt() })
        }
    }
}
