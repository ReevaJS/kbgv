package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

data class BGVBlocks(
    val id: Int,
    val nodes: List<Int>,
    val successors: List<Int>,
) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putInt(id)
        writer.putInt(nodes.size)
        nodes.forEach(writer::putInt)
        writer.putInt(successors.size)
        successors.forEach(writer::putInt)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "blocks")
        put("id", id)
        putJsonArray("nodes") {
            nodes.forEach(::add)
        }
        putJsonArray("successors") {
            successors.forEach(::add)
        }
    }

    override fun toString(): String {
        val nodeStr = nodes.joinToString()
        val followersStr = successors.joinToString()
        return "Blocks {nodes=[$nodeStr], followers=[$followersStr]}"
    }

    companion object : IBGVReader<BGVBlocks> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVBlocks {
            val id = reader.getInt()
            val nodeCount = reader.getInt()
            val nodes = (0 until nodeCount).map { reader.getInt() }
            val successorCount = reader.getInt()
            val successors = (0 until successorCount).map { reader.getInt() }
            return BGVBlocks(id, nodes, successors)
        }
    }
}
