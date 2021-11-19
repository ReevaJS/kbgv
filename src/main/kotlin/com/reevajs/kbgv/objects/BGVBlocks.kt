package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVBlocks(
    val id: Int,
    val nodes: List<Int>,
    val followers: List<Int>,
) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putInt(id)
        writer.putInt(nodes.size)
        nodes.forEach(writer::putInt)
        writer.putInt(followers.size)
        followers.forEach(writer::putInt)
    }

    override fun toString(): String {
        val nodeStr = nodes.joinToString()
        val followersStr = followers.joinToString()
        return "Blocks {nodes=[$nodeStr] followers=[$followersStr]}"
    }

    companion object : IBGVReader<BGVBlocks> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVBlocks {
            val id = reader.getInt()
            val nodeCount = reader.getInt()
            val nodes = (0 until nodeCount).map { reader.getInt() }
            val followerCount = reader.getInt()
            val followers = (0 until followerCount).map { reader.getInt() }
            return BGVBlocks(id, nodes, followers)
        }
    }
}
