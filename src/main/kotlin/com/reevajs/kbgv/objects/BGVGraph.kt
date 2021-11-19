package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVGraph(
    val id: Int,
    val format: String,
    val args: List<IBGVPropObject>,
    val body: BGVGraphBody,
) : IBGVGroupDocumentGraph {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.BEGIN_GRAPH)
        writer.putInt(id)
        writer.putString(format)
        writer.putInt(args.size)
        args.forEach { it.write(writer) }
        body.write(writer)
    }

    companion object : IBGVReader<BGVGraph> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVGraph {
            val id = reader.getInt()
            val format = reader.getString()
            val argCount = reader.getInt()
            val args = (0 until argCount).map { IBGVPropObject.read(reader, context) }
            val body = BGVGraphBody.read(reader, context)
            return BGVGraph(id, format, args, body)
        }
    }
}