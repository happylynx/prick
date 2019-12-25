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

    @JvmOverloads
    fun init(printStream: PrintStream = System.err) {
        printStream.println("""
            Usage:

            prick init [dir]

            It initializes "dir" directory as a prick directory.

            Parameter:
            dir - directory to initialize. Current working directory is used if unspecified.
        """.trimIndent())
    }


    @JvmOverloads
    fun general(printStream: PrintStream = System.err) {
        printStream.println("""
            Usage: prick [--help | --prick-version]
                         commands [args...]
            Commands:
                help    Print command usage help
                init    Initialize directory to be a synchronization root
                sync    Synchronizes selected directories
                snapshot Create a snapshot of content of the prick directory
        """.trimIndent())
    }
}