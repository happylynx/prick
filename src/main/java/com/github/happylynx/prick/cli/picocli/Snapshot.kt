package com.github.happylynx.prick.cli.picocli

import com.github.happylynx.prick.lib.commands.PrickContext
import com.github.happylynx.prick.lib.commands.SnapshotCommand
import picocli.CommandLine
import java.nio.file.Path

@CommandLine.Command(name = "snapshot")
class Snapshot() : Runnable, WithHelp {

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
        if (processHelp(spec)) {
            return
        }
        println("Running snapshot command")
        val normalizedSnapshotRoot = snapshotRoot.toAbsolutePath().normalize()
        println("snapshot root: $normalizedSnapshotRoot")
        println(main.prickDir)
        SnapshotCommand(PrickContext.fromWorkingPath(normalizedSnapshotRoot), normalizedSnapshotRoot).run()
    }
}