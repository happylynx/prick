package com.github.happylynx.prick.cli

import com.github.happylynx.prick.lib.PrickException

object CliUtils {
    private const val GENERAL_ERROR_CODE = 1

    fun die() {
        System.exit(GENERAL_ERROR_CODE)
    }

    fun die(message: String) {
        System.err.println(message)
        die()
    }

    inline fun wrapPrickException(noinline fn: () -> Unit) {
        try {
            fn()
        } catch (ex: PrickException) {
            die(ex.message!!)
        }
    }
}