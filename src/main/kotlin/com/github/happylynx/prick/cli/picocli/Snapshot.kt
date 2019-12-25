package com.github.happylynx.prick.cli.picocli

import com.github.happylynx.prick.cli.CliUtils
import com.github.happylynx.prick.lib.PrickException
import com.github.happylynx.prick.lib.commands.PrickContext
import com.github.happylynx.prick.lib.commands.SnapshotCommand
import picocli.CommandLine
import java.lang.IllegalStateException
import java.nio.file.Path

@CommandLine.Command(name = "snapshot")
class Snapshot() : Runnable {

    @CommandLine.Spec
    lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.ParentCommand
    lateinit var main: Main

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    var help: Boolean = false

    // TODO move to sync
//    @CommandLine.Option(names = ["--ignore-cwd"], defaultValue = "false")
//    var ignoreCwd: Boolean = false

    @CommandLine.Parameters(index = "0", defaultValue = ".")
    lateinit var snapshotRoot: Path

    override fun run() {
        if (PicocliUtils.processHelp(spec)) {
            return
        }
        println("Running snapshot command")
        val normalizedSnapshotRoot = snapshotRoot.toAbsolutePath().normalize()

        val prickContext: PrickContext
        try {
            prickContext = PrickContext.fromWorkingPath(normalizedSnapshotRoot)
        } catch (e: PrickException) {
            CliUtils.die(e.message!!)
            throw IllegalStateException()
        }
        SnapshotCommand(prickContext, normalizedSnapshotRoot).run()
    }
}
