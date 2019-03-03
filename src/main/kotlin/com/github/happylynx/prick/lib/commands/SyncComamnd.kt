package com.github.happylynx.prick.lib.commands

import java.nio.file.Path
import java.util.concurrent.CompletableFuture

class SyncComamnd(prickDirs: List<Path>, private val itemToSync: Path) {
    val ctxs = prickDirs.map(::PrickContext)

    fun run() {
        ctxs
                .map { CompletableFuture.runAsync { SnapshotCommand(it, itemToSync).run() } }
                .forEach { it.join() }
        TODO("to be finished")
    }
}