package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVInputEdgeInfo(
    val indirect: Byte,
    val name: IBGVPoolObject,
    val type: IBGVPoolObject,
) : IBGVWriter {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(indirect)
        name.write(writer)
        type.write(writer)
    }

    override fun toString(): String {
        return "InputEdge {indirect=${indirect.toInt() == 1}, name=$name, type=$type}"
    }

    companion object : IBGVReader<BGVInputEdgeInfo> {
        override fun read(reader: ExpandingByteBuffer): BGVInputEdgeInfo {
            return BGVInputEdgeInfo(reader.getByte(), IBGVPoolObject.read(reader), IBGVPoolObject.read(reader))
        }
    }
}