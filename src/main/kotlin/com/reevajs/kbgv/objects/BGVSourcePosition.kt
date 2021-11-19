package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVSourcePosition(
    val uri: IBGVPoolObject,
    val location: String,
    val line: Int,
    val start: Int,
    val end: Int,
) : IBGVWriter {
    override fun write(writer: ExpandingByteBuffer) {
        uri.write(writer)
        writer.putString(location)
        writer.putInt(line)
        writer.putInt(start)
        writer.putInt(end)
    }

    companion object : IBGVReader<BGVSourcePosition> {
        override fun read(reader: ExpandingByteBuffer): BGVSourcePosition {
            return BGVSourcePosition(
                IBGVPoolObject.read(reader),
                reader.getString(),
                reader.getInt(),
                reader.getInt(),
                reader.getInt(),
            )
        }
    }
}