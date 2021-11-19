package com.reevajs.kbgv

import com.reevajs.kbgv.objects.BGVObject
import java.io.File
import java.nio.ByteOrder

fun main() {
    // val file = File("/home/matthew/code/reeva/graal_dumps/2021.11.18.18.44.15.353/HotSpotCompilation-5162[String.lastIndexOf(int,_int)int].bgv")
    val file = File("/home/matthew/Downloads/fib-js.bgv")
    val bytes = file.readBytes()
    val obj = BGVObject.read(bytes, ByteOrder.BIG_ENDIAN)
    println()
}
