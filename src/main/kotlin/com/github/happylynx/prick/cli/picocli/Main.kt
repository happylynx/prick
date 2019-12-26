package com.github.happylynx.prick.cli.picocli

import com.github.happylynx.prick.cli.CliUtils
import picocli.CommandLine

@CommandLine.Command(
        subcommands = [
            Snapshot::class,
            Init::class
//            Sync::class,
//            Log::class,
//            Prune::class,
//            Verify::class
        ]
)
class Main {

    @CommandLine.Option(names = ["-v", "--version"], versionHelp = true, description = ["Print version"])
    var version: Boolean = false

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Print help"])
    var help: Boolean = false

    fun run(parseResult: CommandLine.ParseResult): Int {
        if (parseResult.isUsageHelpRequested) {
            parseResult.asCommandLineList().first().usage(System.out)
            return CliUtils.SUCCESS_EXIT_CODE
        }
        if (parseResult.isVersionHelpRequested) {
            CliUtils.printVersion(System.out)
            return CliUtils.SUCCESS_EXIT_CODE
        }
        return when (val command = parseResult.asCommandLineList().last()!!.getCommand<Any>()) {
            is Runnable -> {
                command.run()
                CliUtils.SUCCESS_EXIT_CODE
            }
            else -> {
                parseResult.asCommandLineList().first().usage(System.err)
                CliUtils.GENERAL_ERROR_EXIT_CODE
            }
        }
    }

    companion object {
        /**
         * @return exit code
         */
        fun run(args: Array<String>): Int {
            val main = Main()
            val commandLine = CommandLine(main)
            commandLine.setExecutionStrategy(main::run)
            return commandLine.execute(*args)
        }
    }
}