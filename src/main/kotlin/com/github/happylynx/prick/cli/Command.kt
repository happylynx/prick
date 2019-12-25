package com.github.happylynx.prick.cli

import com.github.happylynx.prick.lib.LibUtils
import com.github.happylynx.prick.lib.commands.InitCommand
import com.github.happylynx.prick.lib.commands.PrickContext
import com.github.happylynx.prick.lib.commands.SnapshotCommand
import com.github.happylynx.prick.lib.commands.SyncComamnd
import java.lang.IllegalStateException
import java.nio.file.Path

fun syncCommand(args: Array<String>) {
    // TODO add --subtree option
    if (args.size < 3) {
        Help.sync()
        CliUtils.die()
    }
    val dirs = args.drop(1)
            .map { arg ->
                val path = Path.of(arg)
                if (!LibUtils.isPrickRoot(path)) {
                    CliUtils.die("Path '$path' doesn't reference a prick directory.")
                }
                path.toAbsolutePath()
            }
    SyncComamnd(dirs, Path.of(".")).run()
}

fun initCommand(args: Array<String>) {
    val dir: Path = when (args.size) {
        1 -> Path.of(".")
        2 -> Path.of(args[1])
        else -> {
            Help.init(System.err)
            CliUtils.die()
            throw IllegalStateException()
        }
    }
    InitCommand(dir).run()

}

private fun throwEverytime() {
    throw RuntimeException()
}

fun snapshotCommand(args: Array<String>) {
    // TODO add --prick-root option
    if (args.size > 1) {
        CliUtils.die("Too many arguments of command snapshot")
    }
    val subtree = Path.of(".")
    val ctx = PrickContext.fromWorkingPath(subtree)
    SnapshotCommand(ctx, ctx.rootDir.relativize(subtree)).run()
}
