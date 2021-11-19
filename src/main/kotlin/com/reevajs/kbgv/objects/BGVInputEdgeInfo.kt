package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVInputEdgeInfo(
    val indirect: Boolean,
    val name: IBGVPoolObject,
    val type: IBGVPoolObject,
) : IBGVWriter {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(if (indirect) 1 else 0)
        name.write(writer)
        type.write(writer)
    }

    override fun toString(): String {
        return "InputEdge {indirect=$indirect, name=$name, type=$type}"
    }

    companion object : IBGVReader<BGVInputEdgeInfo> {
        override fun read(reader: ExpandingByteBuffer): BGVInputEdgeInfo {
            val indirect = reader.getByte().toInt() == 1
            return BGVInputEdgeInfo(indirect, IBGVPoolObject.read(reader), IBGVPoolObject.read(reader))
        }
    }
}