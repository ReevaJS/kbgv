package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class BGVInputEdgeInfo(
    val indirect: Boolean,
    val name: IBGVPoolObject,
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
            return BGVInputEdgeInfo(
                indirect,
                IBGVPoolObject.read(reader, context),
                IBGVPoolObject.read(reader, context),
            )
        }
    }
}