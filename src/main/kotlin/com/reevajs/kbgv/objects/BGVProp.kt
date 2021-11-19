package com.reevajs.kbgv.objects

import com.reevajs.kbgv.ExpandingByteBuffer

class BGVProp(val key: IBGVPoolObject, val value: IBGVPropObject) : IBGVWriter {
    override fun write(writer: ExpandingByteBuffer) {
        key.write(writer)
        value.write(writer)
    }

    override fun toString() = "Prop {key=$key, value=$value}"

    companion object : IBGVReader<BGVProp> {
        override fun read(reader: ExpandingByteBuffer): BGVProp {
            return BGVProp(IBGVPoolObject.read(reader), IBGVPropObject.read(reader))
        }
    }
}