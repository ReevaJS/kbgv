package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVGraph(
    val format: String,
    val nodes: Collection<BGVNode>,
    val body: BGVGraphBody,
) : IBGVGroupDocumentGraph {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.BEGIN_GRAPH)
        writer.putString(format)
        writer.putInt(nodes.size)
        nodes.forEach { it.write(writer) }
        body.write(writer)
    }

    companion object : IBGVReader<BGVGraph> {
        override fun read(reader: ExpandingByteBuffer): BGVGraph {
            val format = reader.getString()
            val nodeSize = reader.getInt()
            val nodes = (0 until nodeSize).map { BGVNode.read(reader) }
            val body = BGVGraphBody.read(reader)
            return BGVGraph(format, nodes, body)
        }
    }
}