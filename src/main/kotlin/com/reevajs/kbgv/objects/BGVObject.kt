package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import com.reevajs.kbgv.expect
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 *     BGV {
 *         char[4] = 'BIGV'
 *         sint8 major
 *         sint8 minor
 *         GroupDocumentGraph*
 *     }
 */
data class BGVObject(
    val major: Byte,
    val minor: Byte,
    val children: List<IBGVGroupDocumentGraph>,
) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer, context: Context) {
        writer.putBytesRaw(magic)
        writer.putByte(major)
        writer.putByte(minor)
        children.forEach { it.write(writer, context) }
    }

    override fun toJson(context: Context) = buildJsonObject {
        put("\$type", "BGV")
        put("major", major)
        put("minor", minor)
        putJsonArray("children") {
            children.forEach { add(it.toJson(context)) }
        }
    }

    companion object : IBGVReader<BGVObject> {
        private val magic = "BIGV".toByteArray()

        override fun read(reader: ExpandingByteBuffer, context: Context): BGVObject {
            val bytes = reader.getBytes(4)
            expect(bytes.contentEquals(magic))

            val majorVersion = reader.getByte()
            val minorVersion = reader.getByte()

            val children = mutableListOf<IBGVGroupDocumentGraph>()

            while (!reader.done())
                children.add(IBGVGroupDocumentGraph.read(reader, context))

            return BGVObject(majorVersion, minorVersion, children)
        }
    }
}
