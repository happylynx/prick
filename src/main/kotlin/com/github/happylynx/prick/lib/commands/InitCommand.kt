package com.github.happylynx.prick.lib.commands

import com.github.happylynx.prick.lib.FileNames
import com.github.happylynx.prick.lib.LibUtils
import com.github.happylynx.prick.lib.PrickException
import java.nio.file.Files
import java.nio.file.Path

class InitCommand(val dir: Path) {

    fun run() {
        val isDirectory = Files.isDirectory(dir)
        if (isDirectory) {
            if (LibUtils.isPrickRoot(dir)) {
                return
            }
            initialize()
            return
        }
        if (Files.exists(dir)) {
            throw PrickException("Path '$dir' already exists but it's not a directory.")
        }
        initialize()
    }

    private fun initialize() {
        Files.createDirectories(dir)
        Files.createDirectory(FileNames.prickDir(dir))
    }
}
