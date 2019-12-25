package com.github.happylynx.prick.cli

import picocli.CommandLine
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.util.jar.Manifest


object Command {

    fun run(parseResult: CommandLine.ParseResult) {
        if (parseResult.isUsageHelpRequested) {
            parseResult.asCommandLineList().first().usage(System.out)
            return
        }
        if (parseResult.isVersionHelpRequested) {
            printVersion(System.out)
            return
        }
        when (val command = parseResult.asCommandLineList().last()!!.getCommand<Any>()) {
            is Runnable -> command.run()
        }
    }

    fun printVersion(outputStream: PrintStream) {
        javaClass.classLoader.getResourceAsStream("prick-version").use {
            val versionText = String(it.readAllBytes(), StandardCharsets.UTF_8)
            outputStream.println(versionText)
        }
    }

    fun run(args: Array<String>) {
        if (args.size == 0) {
            Help.general(System.err)
            return
        }
        val firstArgument = args[0]
        if (firstArgument == null || "help" == firstArgument || "--help" == firstArgument) {
            Help.general(System.out)
            return
        }
        if ("--prick-version" == firstArgument) {
            versionOptionCommand(args)
            return
        }
        if ("sync" == firstArgument) {
            syncCommand(args)
            return
        }
        if ("init" == firstArgument) {
            initCommand(args)
            return
        }
        if ("snapshot" == firstArgument) {
            snapshotCommand(args)
            return
        }
        unknownCommand(args)
    }

    private fun versionOptionCommand(args: Array<String>) {
        println("0.1")
    }

    private fun unknownCommand(args: Array<String>) {
        System.err.println("Unknown command: " + args.joinToString(" "))
        Help.general(System.err)
        CliUtils.die()
    }

}
