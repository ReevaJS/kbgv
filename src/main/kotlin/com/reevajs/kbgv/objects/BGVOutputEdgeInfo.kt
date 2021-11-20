package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class BGVOutputEdgeInfo(
    val indirect: Boolean,
    val name: IBGVPoolObject
) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(if (indirect) 1 else 0)
        name.write(writer)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "output_edge_info")
        put("indirect", indirect)
        put("name", name.toJson())
    }

    override fun toString(): String {
        return "OutputEdge {indirect=$indirect, name=$name}"
    }

    companion object : IBGVReader<BGVOutputEdgeInfo> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVOutputEdgeInfo {
            val indirect = reader.getByte().toInt() == 1
            return BGVOutputEdgeInfo(indirect, IBGVPoolObject.read(reader, context))
        }
    }
}