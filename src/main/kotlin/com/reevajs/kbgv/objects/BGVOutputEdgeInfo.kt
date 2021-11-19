package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVOutputEdgeInfo(
    val indirect: Boolean,
    val name: IBGVPoolObject
) : IBGVWriter {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(if (indirect) 1 else 0)
        name.write(writer)
    }

    override fun toString(): String {
        return "OutputEdge {indirect=$indirect, name=$name}"
    }

    companion object : IBGVReader<BGVOutputEdgeInfo> {
        override fun read(reader: ExpandingByteBuffer): BGVOutputEdgeInfo {
            val indirect = reader.getByte().toInt() == 1
            return BGVOutputEdgeInfo(indirect, IBGVPoolObject.read(reader))
        }
    }
}