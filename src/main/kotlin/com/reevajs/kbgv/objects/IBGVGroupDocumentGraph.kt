package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer

/**
 * GroupDocumentGraph {
 *   BeginGroup GroupDocumentGraph CloseGroup | Document | Graph
 * }
 */
sealed interface IBGVGroupDocumentGraph : IBGVWriter {
    companion object : IBGVReader<IBGVGroupDocumentGraph> {
        override fun read(reader: ExpandingByteBuffer): IBGVGroupDocumentGraph {
            return when (reader.getByte()) {
                BGVToken.BEGIN_GROUP -> BGVGroup.read(reader)
                BGVToken.BEGIN_DOCUMENT -> BGVDocument.read(reader)
                BGVToken.BEGIN_GRAPH -> BGVGraph.read(reader)
                else -> throw IllegalStateException()
            }
        }
    }
}
