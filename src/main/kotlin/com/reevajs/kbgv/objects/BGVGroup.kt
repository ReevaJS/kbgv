package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer

/**
 * BeginGroup {
 *   sint8 token = BEGIN_GROUP
 *   PoolObject name
 *   PoolObject method
 *   sint32 bci
 *   Props props
 * }
 */
data class BGVGroup(
    val name: IBGVPoolObject,
    val shortName: IBGVPoolObject,
    val method: IBGVPoolObject,
    val bci: Int,
    val props: BGVProps,
    val groups: List<IBGVGroupDocumentGraph>,
) : IBGVGroupDocumentGraph {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.BEGIN_GROUP)
        name.write(writer)
        shortName.write(writer)
        method.write(writer)
        writer.putInt(bci)
        props.write(writer)
        groups.forEach { it.write(writer) }
        writer.putByte(BGVToken.CLOSE_GROUP)
    }

    companion object : IBGVReader<BGVGroup> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVGroup {
            val name = IBGVPoolObject.read(reader, context)
            val shortName = IBGVPoolObject.read(reader, context)
            val method = IBGVPoolObject.read(reader, context)
            val bci = reader.getInt()
            val props = BGVProps.read(reader, context)

            val groups = mutableListOf<IBGVGroupDocumentGraph>()
            while (reader.peekByte() != BGVToken.CLOSE_GROUP)
                groups.add(IBGVGroupDocumentGraph.read(reader, context))
            reader.getByte()

            return BGVGroup(name, shortName, method, bci, props, groups)
        }
    }
}