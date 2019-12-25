package com.github.happylynx.prick.cli

import com.github.happylynx.prick.lib.PrickException
import java.io.PrintStream
import java.nio.charset.StandardCharsets

object CliUtils {
    private const val GENERAL_ERROR_CODE = 1

    fun die() {
        System.exit(GENERAL_ERROR_CODE)
    }

    fun die(message: String) {
        System.err.println(message)
        die()
    }

    fun wrapPrickException(fn: () -> Unit) {
        try {
            fn()
        } catch (ex: PrickException) {
            die(ex.message!!)
        }
    }

    fun printVersion(printStream: PrintStream) {
        printStream.println(getVersion())
    }

    private fun getVersion(): String {
        javaClass.getResourceAsStream("prick-version").use {
            return String(it.readAllBytes(), StandardCharsets.UTF_8)
        }
    }
}