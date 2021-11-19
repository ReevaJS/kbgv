package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVOutputEdgeInfo(
    val indirect: Byte,
    val name: IBGVPoolObject
) : IBGVWriter {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(indirect)
        name.write(writer)
    }

    override fun toString(): String {
        return "OutputEdge {indirect=${indirect.toInt() == 1}, name=$name}"
    }

    companion object : IBGVReader<BGVOutputEdgeInfo> {
        override fun read(reader: ExpandingByteBuffer): BGVOutputEdgeInfo {
            return BGVOutputEdgeInfo(reader.getByte(), IBGVPoolObject.read(reader))
        }
    }
}