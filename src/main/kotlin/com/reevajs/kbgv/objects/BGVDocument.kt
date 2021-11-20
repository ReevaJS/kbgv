package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 *     Document {
 *         sint8 token = BEGIN_DOCUMENT
 *         Props props
 *     }
 */
data class BGVDocument(val props: BGVProps) : IBGVGroupDocumentGraph {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.BEGIN_DOCUMENT)
        props.write(writer)
    }

    override fun toJson() = buildJsonObject {
        put("\$type", "document")
        put("props", props.toJson())
    }

    override fun toString() = "Document {$props}"

    companion object : IBGVReader<BGVDocument> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVDocument {
            return BGVDocument(BGVProps.read(reader, context))
        }
    }
}