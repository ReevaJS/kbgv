package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import com.reevajs.kbgv.expectIs
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 *     OutputEdgeInfo {
 *         sint8 indirect
 *         PoolObject name
 *     }
 */
data class BGVOutputEdgeInfo(
    val indirect: Boolean,
    val name: BGVStringPool
) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer, context: Context) {
        writer.putByte(if (indirect) 1 else 0)
        name.write(writer, context)
    }

    override fun toJson(context: Context) = buildJsonObject {
        put("\$type", "output_edge_info")
        put("indirect", indirect)
        put("name", name.toJson(context))
    }

    override fun toString(): String {
        return "OutputEdge {indirect=$indirect, name=$name}"
    }

    companion object : IBGVReader<BGVOutputEdgeInfo> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVOutputEdgeInfo {
            val indirect = reader.getByte().toInt() == 1
            val name = IBGVPoolObject.read(reader, context)
            expectIs<BGVStringPool>(name)
            return BGVOutputEdgeInfo(indirect, name)
        }
    }
}