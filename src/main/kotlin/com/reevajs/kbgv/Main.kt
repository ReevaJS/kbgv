package com.reevajs.kbgv

import com.reevajs.kbgv.objects.BGVObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.ByteOrder

val JSON = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
    allowSpecialFloatingPointValues = true
}

fun main() {
    val file = File("/home/matthew/code/seafoam/examples/fib-js.bgv")
    val bytes = file.readBytes()
    val obj = BGVObject.read(bytes, ByteOrder.BIG_ENDIAN)
    val element = obj.toJson()
    val out = File("./out.json")
    out.writeText(JSON.encodeToString(element))
}
