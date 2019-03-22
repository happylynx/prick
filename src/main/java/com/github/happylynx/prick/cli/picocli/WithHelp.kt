package com.github.happylynx.prick.cli.picocli

import picocli.CommandLine

internal interface WithHelp {

    fun processHelp(spec: CommandLine.Model.CommandSpec): Boolean {
        if (spec.commandLine().isUsageHelpRequested) {
            spec.commandLine().usage(System.out)
            return true
        }
        return false
    }
}