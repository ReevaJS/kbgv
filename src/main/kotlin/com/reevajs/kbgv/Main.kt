package com.reevajs.kbgv

import com.reevajs.kbgv.objects.BGVObject
import com.reevajs.kbgv.objects.Context
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.ByteOrder
import java.nio.file.Files

@OptIn(ExperimentalSerializationApi::class)
val JSON = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
    allowSpecialFloatingPointValues = true
}

fun read(file: File): BGVObject {
    return BGVObject.read(file.readBytes(), ByteOrder.BIG_ENDIAN)
}

fun write(obj: BGVObject, file: File) {
    file.writeBytes(obj.write(ByteOrder.BIG_ENDIAN))
}

fun writeJson(obj: BGVObject, file: File) {
    file.writeText(JSON.encodeToString(obj.toJson()))
}

fun main() {
    val file = File("/home/matthew/code/seafoam/examples/fib-js.bgv")
    val tmp1 = File("/home/matthew/Desktop/tmp1.json")
    val tmp2 = File("/home/matthew/Desktop/tmp2.json")

    var obj = read(file)
    write(obj, tmp1)

    obj = read(tmp1)
    write(obj, tmp2)
}
