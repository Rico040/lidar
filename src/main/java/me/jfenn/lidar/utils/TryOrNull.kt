package me.jfenn.lidar.utils

import org.apache.commons.lang3.exception.ContextedRuntimeException

private fun printException(e: Throwable) {
    println("EXCEPTION CAUGHT (tryOrNull): ${e.javaClass.name} ${e.message}")
    e.cause?.let { cause ->
        print("    CAUSED BY: ")
        printException(cause)
        cause.printStackTrace()
    }

    if (e is ContextedRuntimeException) {
        e.contextEntries.forEach { (key, value) ->
            println("    ERROR CONTEXT '$key': '$value'")
        }
    }
}

fun <T> tryOrNull(fn: () -> T) : T? {
    return try {
        fn()
    } catch (e: Exception) {
        //printException(e)
        null
    }
}

fun <T> tryOrEmpty(fn: () -> List<T>?) : List<T> {
    return tryOrNull(fn) ?: listOf()
}
