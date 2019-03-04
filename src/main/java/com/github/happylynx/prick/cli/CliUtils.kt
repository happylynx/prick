package com.github.happylynx.prick.cli

object CliUtils {
    private const val GENERAL_ERROR_CODE = 1

    fun die() {
        System.exit(GENERAL_ERROR_CODE)
    }

    fun die(message: String) {
        System.err.println(message)
        die()
    }
}