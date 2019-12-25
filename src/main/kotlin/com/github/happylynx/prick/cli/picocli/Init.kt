package com.github.happylynx.prick.cli.picocli

import com.github.happylynx.prick.cli.CliUtils
import com.github.happylynx.prick.lib.commands.InitCommand
import picocli.CommandLine
import java.nio.file.Path

@CommandLine.Command(name = "init", description = ["Init initializes new prick directory."])
class Init : Runnable {

    @CommandLine.ParentCommand
    lateinit var parent: Main

    @CommandLine.Spec
    lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    var help: Boolean = false

    @CommandLine.Parameters(index = "0", arity = "0..1", defaultValue = ".")
    lateinit var path: Path

    override fun run() {
        if (PicocliUtils.processHelp(spec)) {
            return
        }
        val normalizedPath = path.toAbsolutePath().normalize()
        CliUtils.wrapPrickException { InitCommand(normalizedPath).run() }
    }
}