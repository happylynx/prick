package com.github.happylynx.prick.cli.picocli

import picocli.CommandLine

object PicocliUtils {
    fun processHelp(spec: CommandLine.Model.CommandSpec): Boolean {
        if (spec.commandLine().isUsageHelpRequested) {
            spec.commandLine().usage(System.out)
            return true
        }
        return false
    }
}