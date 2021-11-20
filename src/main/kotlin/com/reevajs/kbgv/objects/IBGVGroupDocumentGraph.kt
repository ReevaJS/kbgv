package com.reevajs.kbgv.objects

import com.reevajs.kbgv.BGVToken
import com.reevajs.kbgv.ExpandingByteBuffer
import com.reevajs.kbgv.unreachable

/**
 *     GroupDocumentGraph {
 *         BeginGroup GroupDocumentGraph* CloseGroup | Document | Graph
 *     }
 */
sealed interface IBGVGroupDocumentGraph : IBGVObject {
    companion object : IBGVReader<IBGVGroupDocumentGraph> {
        override fun read(reader: ExpandingByteBuffer, context: Context): IBGVGroupDocumentGraph {
            return when (reader.getByte()) {
                BGVToken.BEGIN_GROUP -> BGVGroup.read(reader, context)
                BGVToken.BEGIN_DOCUMENT -> BGVDocument.read(reader, context)
                BGVToken.BEGIN_GRAPH -> BGVGraph.read(reader, context)
                else -> unreachable()
            }
        }
    }
}
