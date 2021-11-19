package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface IBGVWriter {
    fun write(writer: ExpandingByteBuffer)

    fun write(order: ByteOrder): ByteArray {
        val buf = ExpandingByteBuffer(order)
        write(buf)
        return buf.bytes()
    }
}

interface IBGVReader<T> {
    fun read(reader: ExpandingByteBuffer): T

    fun read(array: ByteArray, order: ByteOrder): T {
        return read(ExpandingByteBuffer(array, order))
    }
}
