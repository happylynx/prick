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

    @CommandLine.Option(names = ["--prick-dir"], description = ["Custom prick root directory"])
    var prickDir: String? = null

    fun run(parseResult: CommandLine.ParseResult) {
        if (parseResult.isUsageHelpRequested) {
            parseResult.asCommandLineList().first().usage(System.out)
            return
        }
        if (parseResult.isVersionHelpRequested) {
            CliUtils.printVersion(System.out)
            return
        }
        when (val command = parseResult.asCommandLineList().last()!!.getCommand<Any>()) {
            is Runnable -> command.run()
        }

    }

    companion object {
        fun run(args: Array<String>) {
            val main = Main()
            CommandLine(main).parseWithHandler(main::run, args)
        }
    }
}