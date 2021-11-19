package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVNode(
    val id: Int,
    val nodeClass: BGVNodeClassPool,
    val hasPredecessor: Boolean,
    val props: BGVProps,
    val edgesIn: Collection<BGVEdge>,
    val edgesOut: Collection<BGVEdge>,
) : IBGVWriter {
    init {
        if (nodeClass.inputs.size != edgesIn.size)
            throw IllegalArgumentException()
        if (nodeClass.outputs.size != edgesOut.size)
            throw IllegalArgumentException()
    }

    override fun write(writer: ExpandingByteBuffer) {
        writer.putInt(id)
        nodeClass.write(writer)
        writer.putByte(if (hasPredecessor) 1 else 0)
        props.write(writer)

        edgesIn.forEach { it.write(writer) }
        edgesOut.forEach { it.write(writer) }
    }

    companion object : IBGVReader<BGVNode> {
        override fun read(reader: ExpandingByteBuffer): BGVNode {
            val id = reader.getInt()
            val nodeClass = BGVNodeClassPool.read(reader)
            val hasPredecessor = reader.getByte().toInt() != 0
            val props = BGVProps.read(reader)
            val edgesIn = (0 until nodeClass.inputs.size).map { BGVEdge.read(reader) }
            val edgesOut = (0 until nodeClass.outputs.size).map { BGVEdge.read(reader) }
            return BGVNode(id, nodeClass, hasPredecessor, props, edgesIn, edgesOut)
        }
    }
}