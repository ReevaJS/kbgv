package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVGraphBody(
    val props: BGVProps,
    val nodes: List<BGVNode>,
    val blocks: List<BGVBlocks>,
) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer) {
        props.write(writer)
        writer.putInt(nodes.size)
        nodes.forEach { it.write(writer) }
        writer.putInt(blocks.size)
        blocks.forEach { it.write(writer) }
    }

    companion object : IBGVReader<BGVGraphBody> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVGraphBody {
            val props = BGVProps.read(reader, context)
            val nodeLength = reader.getInt()
            val nodes = (0 until nodeLength).map { BGVNode.read(reader, context) }
            val blockLength = reader.getInt()
            val blocks = (0 until blockLength).map { BGVBlocks.read(reader, context) }
            return BGVGraphBody(props, nodes, blocks)
        }
    }
}