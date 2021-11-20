package com.reevajs.kbgv

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <reified T : Any?> expectIs(obj: Any?) {
    contract { returns() implies (obj is T) }
    if (obj !is T) {
        val objType = if (obj == null) "null" else obj::class.java.simpleName
        throw IllegalArgumentException("Expected type ${T::class.java}, found type $objType")
    }
}
