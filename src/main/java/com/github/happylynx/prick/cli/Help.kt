package com.github.happylynx.prick.cli

import java.io.PrintStream


object Help {
    fun sync() {
        println("""
            Usage:

            prick sync dir1 dir2 ...

            It synchronizes content of prick directories. Arguments dir1, dir2 and other have
            to reference an initialized prick directory.

            Parameters:
            dirN - directories to run synchronization among
        """.trimIndent())
    }

    fun init(printStream: PrintStream = System.err) {
        printStream.println("""
            Usage:

            prick init [dir]

            It initializes "dir" directory as a prick directory.

            Parameter:
            dir - directory to initialize. Current working directory is used if unspecified.
        """.trimIndent())
    }
}