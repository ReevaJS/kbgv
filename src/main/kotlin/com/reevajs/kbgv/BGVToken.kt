package com.reevajs.kbgv

object BGVToken {
    const val BEGIN_GROUP = 0x00.toByte()
    const val BEGIN_GRAPH = 0x01.toByte()
    const val CLOSE_GROUP = 0x02.toByte()
    const val BEGIN_DOCUMENT = 0x03.toByte()

    const val POOL_NEW = 0x00.toByte()
    const val POOL_STRING = 0x01.toByte()
    const val POOL_ENUM = 0x02.toByte()
    const val POOL_CLASS = 0x03.toByte()
    const val POOL_METHOD = 0x04.toByte()
    const val POOL_NULL = 0x05.toByte()
    const val POOL_NODE_CLASS = 0x06.toByte()
    const val POOL_FIELD = 0x07.toByte()
    const val POOL_SIGNATURE = 0x08.toByte()
    const val POOL_NODE_SOURCE_POSITION = 0x09.toByte()
    const val POOL_NODE = 0x0a.toByte()

    const val PROPERTY_POOL = 0x00.toByte()
    const val PROPERTY_INT = 0x01.toByte()
    const val PROPERTY_LONG = 0x02.toByte()
    const val PROPERTY_DOUBLE = 0x03.toByte()
    const val PROPERTY_FLOAT = 0x04.toByte()
    const val PROPERTY_TRUE = 0x05.toByte()
    const val PROPERTY_FALSE = 0x06.toByte()
    const val PROPERTY_ARRAY = 0x07.toByte()
    const val PROPERTY_SUBGRAPH = 0x08.toByte()

    const val KLASS = 0x00.toByte()
    const val ENUM_KLASS = 0x01.toByte()

    const val MAJOR_VERSION = 8.toByte()
    const val MINOR_VERSION = 0.toByte()
}
