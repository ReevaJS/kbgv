package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVNode(
    val id: Int,
    val nodeClass: BGVNodeClassPool,
    val hasPredecessor: Boolean,
    val props: BGVProps,
    val edgesIn: List<IBGVEdge>,
    val edgesOut: List<IBGVEdge>,
) : IBGVObject {
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

        edgesIn.forEachIndexed { index, edge ->
            val isIndirect = nodeClass.inputs[index].indirect
            if (isIndirect != (edge is BGVIndirectEdge))
                throw IllegalStateException()
            edge.write(writer)
        }

        edgesOut.forEachIndexed { index, edge ->
            val isIndirect = nodeClass.outputs[index].indirect
            if (isIndirect != (edge is BGVIndirectEdge))
                throw IllegalStateException()
            edge.write(writer)
        }
    }

    companion object : IBGVReader<BGVNode> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNode {
            val id = reader.getInt()
            val nodeClass = IBGVPoolObject.read(reader, context)
            if (nodeClass !is BGVNodeClassPool)
                throw IllegalStateException()

            val hasPredecessor = reader.getByte().toInt() != 0
            val props = BGVProps.read(reader, context)
            val edgesIn = (0 until nodeClass.inputs.size).map {
                if (nodeClass.inputs[it].indirect) {
                    BGVIndirectEdge.read(reader, context)
                } else BGVDirectEdge.read(reader, context)
            }
            val edgesOut = (0 until nodeClass.outputs.size).map {
                if (nodeClass.outputs[it].indirect) {
                    BGVIndirectEdge.read(reader, context)
                } else BGVDirectEdge.read(reader, context)
            }
            return BGVNode(id, nodeClass, hasPredecessor, props, edgesIn, edgesOut)
        }
    }
}
