package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import java.nio.ByteOrder

class Context {
    private val poolObjects = mutableMapOf<UShort, IBGVPoolObject>()

    operator fun contains(id: UShort) = id in poolObjects

    operator fun get(id: UShort) = poolObjects[id]!!

    operator fun set(id: UShort, value: IBGVPoolObject) {
        if (id in this)
            throw IllegalArgumentException()
        poolObjects[id] = value
    }
}

interface IBGVObject {
    fun write(writer: ExpandingByteBuffer)

    fun write(order: ByteOrder): ByteArray {
        val buf = ExpandingByteBuffer(order)
        write(buf)
        return buf.bytes()
    }
}

interface IBGVReader<T> {
    fun read(reader: ExpandingByteBuffer, context: Context): T

    fun read(array: ByteArray, order: ByteOrder): T {
        return read(ExpandingByteBuffer(array, order), Context())
    }
}