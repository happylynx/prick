package com.github.happylynx.prick.lib.commands

import com.github.happylynx.prick.lib.FileNames
import com.github.happylynx.prick.lib.LibUtils
import com.github.happylynx.prick.lib.TreeComparator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

internal class InitCommandTest {

    @Test
    fun emptyDirectory(@TempDir tmpDir: Path) {
        InitCommand(tmpDir).run()
        assertTrue(LibUtils.isPrickRoot(tmpDir))
    }

    @Test
    fun existingPrickDirectory(@TempDir dir: Path) {
        initializePrickDir(dir)
        val treeComparator = TreeComparator(dir)
        InitCommand(dir).run()
        val differences = treeComparator.compareTo(dir)
        assertTrue(differences.isEmpty())
    }

}

private fun initializePrickDir(dir: Path) {
    val prickDir = FileNames.prickDir(dir)
    Files.createDirectory(prickDir)
    Files.writeString(prickDir.resolve("file.txt"), "foo")
}