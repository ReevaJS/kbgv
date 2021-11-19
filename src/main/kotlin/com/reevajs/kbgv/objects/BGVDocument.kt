package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer

data class BGVDocument(val props: BGVProps) : IBGVGroupDocumentGraph {
    override fun write(writer: ExpandingByteBuffer) {
        writer.putByte(BGVToken.BEGIN_DOCUMENT)
        props.write(writer)
    }

    override fun toString(): String {
        return "Document ($props)"
    }

    companion object : IBGVReader<BGVDocument> {
        override fun read(reader: ExpandingByteBuffer, context: Context): BGVDocument {
            return BGVDocument(BGVProps.read(reader, context))
        }
    }
}