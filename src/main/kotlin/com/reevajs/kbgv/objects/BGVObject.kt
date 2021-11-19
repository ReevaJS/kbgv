package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

/**
 * BGV {
 *   char[4] = 'BIGV'
 *   sint8 major
 *   sint8 minor
 *   GroupDocumentGraph
 * }
 */
data class BGVObject(
    val major: Byte,
    val minor: Byte,
    val components: List<IBGVGroupDocumentGraph>,
) : IBGVObject {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putBytes(magic)
        writer.putByte(major)
        writer.putByte(minor)
        components.forEach { it.write(writer) }
    }

    companion object : IBGVReader<BGVObject> {
        private val magic = "BIGV".toByteArray()

        override fun read(reader: ExpandingByteBuffer, context: Context): BGVObject {
            val bytes = reader.getBytes(4)
            if (!bytes.contentEquals(magic))
                throw IllegalStateException()

            val majorVersion = reader.getByte()
            val minorVersion = reader.getByte()

            val components = mutableListOf<IBGVGroupDocumentGraph>()

            while (!reader.done())
                components.add(IBGVGroupDocumentGraph.read(reader, context))

            return BGVObject(majorVersion, minorVersion, components)
        }
    }
}
