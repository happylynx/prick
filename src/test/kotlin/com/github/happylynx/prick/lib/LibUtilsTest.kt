package com.github.happylynx.prick.lib

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

internal class LibUtilsTest {

    @Test
    fun isPrickRoot_emptyDir(@TempDir dir: Path) {
        assertFalse(LibUtils.isPrickRoot(dir))
    }

    @Test
    fun isPrickRoot_differentContent(@TempDir dir: Path) {
        Files.createDirectory(dir.resolve(".otherPrick"))
        assertFalse(LibUtils.isPrickRoot(dir))
    }

    @Test
    fun isPrickRoot_valid(@TempDir dir: Path) {
        Files.createDirectory(dir.resolve(".prick"))
        assertTrue(LibUtils.isPrickRoot(dir))
    }

    @Test
    fun isPrickRoot_file(@TempDir dir: Path) {
        val file = dir.resolve("file.txt")
        Files.writeString(file, "foo")
        assertFalse(LibUtils.isPrickRoot(file))
    }
}