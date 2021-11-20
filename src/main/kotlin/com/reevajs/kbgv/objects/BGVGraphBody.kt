package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 *     GraphBody {
 *         Props props
 *         sint32 nodes_count
 *         Node[nodes_count] nodes
 *         sint32 blocks_count
 *         Blocks[blocks_count] blocks
 *     }
 */
data class BGVGraphBody(
    val props: BGVProps,
    val nodes: List<BGVNode>,
    val blocks: List<BGVBlocks>,
) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer, context: Context) {
        props.write(writer, context)
        writer.putInt(nodes.size)
        nodes.forEach { it.write(writer, context) }
        writer.putInt(blocks.size)
        blocks.forEach { it.write(writer, context) }
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "graph_body")
        put("props", props.toJson())
        putJsonArray("nodes") {
            nodes.forEach { add(it.toJson()) }
        }
        putJsonArray("blocks") {
            blocks.forEach { add(it.toJson()) }
        }
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