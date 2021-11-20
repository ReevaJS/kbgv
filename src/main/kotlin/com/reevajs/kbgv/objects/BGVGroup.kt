package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer
import com.reevajs.kbgv.expectIs
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 *     BeginGroup {
 *         sint8 token = BEGIN_GROUP
 *         PoolObject name
 *         PoolObject short_name
 *         PoolObject method
 *         sint32 bci
 *         Props props
 *     }
 */
data class BGVGroup(
    val name: BGVStringPool,
    val shortName: BGVStringPool,
    val method: BGVMethodPool?,
    val bci: Int,
    val props: BGVProps,
    val children: List<IBGVGroupDocumentGraph>,
) : IBGVGroupDocumentGraph {
    override fun write(writer: ExpandingByteBuffer, context: Context) {
        writer.putByte(BGVToken.BEGIN_GROUP)
        name.write(writer, context)
        shortName.write(writer, context)
        method.write(writer, context)
        writer.putInt(bci)
        props.write(writer, context)
        children.forEach { it.write(writer, context) }
        writer.putByte(BGVToken.CLOSE_GROUP)
    }

    override fun toJson(context: Context) = buildJsonObject {
        put("\$type", "group")
        put("name", name.toJson(context))
        put("short_name", shortName.toJson(context))
        put("method", method.toJson(context))
        put("bci", bci)
        put("props", props.toJson(context))
        putJsonArray("children") {
            children.forEach { add(it.toJson(context)) }
        }
    }

    companion object : IBGVReader<BGVGroup> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVGroup {
            val name = IBGVPoolObject.read(reader, context)
            val shortName = IBGVPoolObject.read(reader, context)
            val method = IBGVPoolObject.read(reader, context).toNullType()
            val bci = reader.getInt()
            val props = BGVProps.read(reader, context)

            val children = mutableListOf<IBGVGroupDocumentGraph>()
            while (reader.peekByte() != BGVToken.CLOSE_GROUP)
                children.add(IBGVGroupDocumentGraph.read(reader, context))
            reader.getByte()

            expectIs<BGVStringPool>(name)
            expectIs<BGVStringPool>(shortName)
            expectIs<BGVMethodPool?>(method)

            return BGVGroup(name, shortName, method, bci, props, children)
        }
    }
}