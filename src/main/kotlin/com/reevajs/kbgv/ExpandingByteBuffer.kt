package com.reevajs.kbgv

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ExpandingByteBuffer {
    private var buf: ByteBuffer

    constructor(order: ByteOrder) {
        buf = ByteBuffer.allocate(10_000).order(order)
    }

    constructor(array: ByteArray, order: ByteOrder) {
        buf = ByteBuffer.wrap(array).order(order)
    }

    fun done() = buf.position() == buf.limit()

    fun peekByte(): Byte = buf.get(buf.position())

    fun getByte(): Byte = buf.get()

    fun getChar(): Char = buf.char

    fun getShort(): Short = buf.short

    fun getInt(): Int = buf.int

    fun getLong(): Long = buf.long

    fun getFloat(): Float = buf.float

    fun getDouble(): Double = buf.double

    fun getBytes(): ByteArray {
        val count = getInt()
        if (count == -1)
            return ByteArray(0)
        return getBytes(count)
    }

    fun getBytes(count: Int): ByteArray {
        val array = ByteArray(count)
        buf.get(array)
        return array
    }

    fun getString(): String {
        val array = getBytes()
        return array.toString(Charsets.UTF_8)
    }

    fun putByte(byte: Byte) = apply {
        ensureCapacity(Byte.SIZE_BYTES)
        buf.put(byte)
    }

    fun putChar(char: Char) = apply {
        ensureCapacity(Char.SIZE_BYTES)
        buf.putChar(char)
    }

    fun putShort(short: Short) = apply {
        ensureCapacity(Short.SIZE_BYTES)
        buf.putShort(short)
    }

    fun putInt(int: Int) = apply {
        ensureCapacity(Int.SIZE_BYTES)
        buf.putInt(int)
    }

    fun putLong(long: Long) = apply {
        ensureCapacity(Long.SIZE_BYTES)
        buf.putLong(long)
    }

    fun putFloat(float: Float) = apply {
        ensureCapacity(Float.SIZE_BYTES)
        buf.putFloat(float)
    }

    fun putDouble(double: Double) = apply {
        ensureCapacity(Double.SIZE_BYTES)
        buf.putDouble(double)
    }

    fun putBytes(bytes: ByteArray) = apply {
        ensureCapacity(bytes.size + 4)
        buf.putInt(bytes.size)
        buf.put(bytes)
    }

    fun putString(string: String) = apply {
        ensureCapacity(string.length + 4)
        putBytes(string.toByteArray())
    }

    fun bytes(): ByteArray = let {
        expect(buf.hasArray())
        buf.array()
    }

    private fun ensureCapacity(capacity: Int) {
        if (buf.limit() < buf.position() + capacity) {
            val newBuf = ByteBuffer.allocate(buf.limit() * 2).order(buf.order())
            newBuf.put(buf.array(), 0, buf.position())
            buf = newBuf
        }
    }
}
