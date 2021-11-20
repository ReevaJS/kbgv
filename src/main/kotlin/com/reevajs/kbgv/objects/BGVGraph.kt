package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 *     Graph {
 *         sint8 token = BEGIN_GRAPH
 *         sint32 id
 *         String format
 *         sint32 args_count
 *         PropObject[args_count] args
 *         GraphBody body
 *     }
 */
data class BGVGraph(
    val id: Int,
    val format: String,
    val args: List<IBGVPropObject>,
    val body: BGVGraphBody,
) : IBGVGroupDocumentGraph {
    override fun write(writer: ExpandingByteBuffer, context: Context) {
        writer.putByte(BGVToken.BEGIN_GRAPH)
        writer.putInt(id)
        writer.putString(format)
        writer.putInt(args.size)
        args.forEach { it.write(writer, context) }
        body.write(writer, context)
    }

    override fun toJson(context: Context) = buildJsonObject {
        put("\$type", "graph")
        put("id", id)
        put("format", format)
        putJsonArray("args") {
            args.forEach { add(it.toJson(context)) }
        }
        put("body", body.toJson(context))
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