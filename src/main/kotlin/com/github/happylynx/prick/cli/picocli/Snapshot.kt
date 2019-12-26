package com.github.happylynx.prick.cli.picocli

import com.github.happylynx.prick.cli.CliUtils
import com.github.happylynx.prick.lib.PrickException
import com.github.happylynx.prick.lib.commands.PrickContext
import com.github.happylynx.prick.lib.commands.SnapshotCommand
import picocli.CommandLine
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Path

@CommandLine.Command(name = "snapshot")
class Snapshot() : Runnable {

    @CommandLine.Spec
    lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.ParentCommand
    lateinit var main: Main

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    var help: Boolean = false

    @CommandLine.Option(names = ["-d", "--directory"], description = ["Custom directory used instead of current working directory"])
    var customPrickDir: String? = null

    // TODO move to sync
//    @CommandLine.Option(names = ["--ignore-cwd"], defaultValue = "false")
//    var ignoreCwd: Boolean = false

    @CommandLine.Parameters(index = "0", defaultValue = ".", description = [
        "Path defining snapshot root",
        "The path can be absolute of relative to current directory (or directory specified by --directory option)"])
    lateinit var subtree: Path

    override fun run() {
        if (PicocliUtils.processHelp(spec)) {
            return
        }
        println("Running snapshot command")
        val customPrickDirPath = validateCustomPrickDir()
        val workingDirectory = customPrickDirPath ?: Path.of("")!!
        val normalizedSnapshotRoot = if (subtree.isAbsolute) {
            subtree
        } else {
            workingDirectory.resolve(subtree)
        }.normalize()

        val prickContext: PrickContext
        try {
            prickContext = PrickContext.fromWorkingPath(normalizedSnapshotRoot)
        } catch (e: PrickException) {
            CliUtils.die(e.message!!)
            throw IllegalStateException()
        }
        val relativeSnapshotRoot = prickContext.rootDir.relativize(normalizedSnapshotRoot)
        SnapshotCommand(prickContext, relativeSnapshotRoot).run()
    }

    private fun validateCustomPrickDir(): Path? {
        if (customPrickDir == null) {
            return null
        }
        val path = Path.of(customPrickDir)
        val isDir = Files.isDirectory(path)
        if (!isDir) {
            CliUtils.die("Custom directory '$customPrickDir' is not a directory")
            throw IllegalStateException()
        }
        return path
    }
}
