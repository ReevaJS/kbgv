package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import com.reevajs.kbgv.expectIs
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 *     InputEdgeInfo {
 *         sint8 indirect
 *         PoolObject name
 *         PoolObject type
 *     }
 */
data class BGVInputEdgeInfo(
    val indirect: Boolean,
    val name: BGVStringPool,
    val type: IBGVPoolObject,
) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(if (indirect) 1 else 0)
        name.write(writer)
        type.write(writer)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "input_edge_info")
        put("indirect", indirect)
        put("name", name.toJson())
        put("type", type.toJson())
    }

    override fun toString(): String {
        return "InputEdge {indirect=$indirect, name=$name, type=$type}"
    }

    companion object : IBGVReader<BGVInputEdgeInfo> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVInputEdgeInfo {
            val indirect = reader.getByte().toInt() == 1
            val name = IBGVPoolObject.read(reader, context)
            expectIs<BGVStringPool>(name)
            val type = IBGVPoolObject.read(reader, context)
            return BGVInputEdgeInfo(indirect, name, type)
        }
    }
}