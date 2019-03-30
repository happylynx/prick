package com.github.happylynx.prick.lib.commands

import com.github.happylynx.prick.lib.FileNames
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class IndexToTreeTest {

    @Test
    fun emptyIndex(@TempDir dir: Path) {
        assertTrue(true)
    }

    @Test
    fun emptyDir(@TempDir dir: Path) {
        InitCommand(dir).run()
        val snapshotCommand = SnapshotCommand(PrickContext(dir), Path.of(""))
        snapshotCommand.run()
        assertTrue(Files.isRegularFile(FileNames.index(dir)))
        assertTrue(Files.isRegularFile(FileNames.head(dir)))
        assertTrue(Files.isDirectory(FileNames.objects(dir)))
    }
}