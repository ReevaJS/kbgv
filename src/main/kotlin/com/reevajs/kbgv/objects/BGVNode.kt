package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import com.reevajs.kbgv.expect
import com.reevajs.kbgv.expectIs
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 *     Node {
 *         sint32 id
 *         PoolObject node_class
 *         bool has_predecessor
 *         Props props
 *         Edge[node_class.inputs.size] edges_in
 *         Edge[node_class.outputs.size] edges_out
 *     }
 */
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

    override fun write(writer: ExpandingByteBuffer, context: Context) {
        writer.putInt(id)
        nodeClass.write(writer, context)
        writer.putByte(if (hasPredecessor) 1 else 0)
        props.write(writer, context)

        edgesIn.forEachIndexed { index, edge ->
            expect(nodeClass.inputs[index].indirect == (edge is BGVIndirectEdge))
            edge.write(writer, context)
        }

        edgesOut.forEachIndexed { index, edge ->
            expect(nodeClass.outputs[index].indirect == (edge is BGVIndirectEdge))
            edge.write(writer, context)
        }
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "node")
        put("id", id)
        put("node_class", nodeClass.toJson())
        put("has_predecessor", hasPredecessor)
        put("props", props.toJson())
        putJsonArray("edges_in") {
            edgesIn.forEach { add(it.toJson()) }
        }
        putJsonArray("edges_out") {
            edgesOut.forEach { add(it.toJson()) }
        }
    }

    companion object : IBGVReader<BGVNode> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVNode {
            val id = reader.getInt()
            val nodeClass = IBGVPoolObject.read(reader, context)
            expectIs<BGVNodeClassPool>(nodeClass)

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
