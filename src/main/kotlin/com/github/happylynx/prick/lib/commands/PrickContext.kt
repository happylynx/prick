package com.github.happylynx.prick.lib.commands

import com.github.happylynx.prick.lib.FileNames
import com.github.happylynx.prick.lib.LockedByOtherProcessException
import com.github.happylynx.prick.lib.LibUtils
import com.github.happylynx.prick.lib.PrickException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.*

class PrickContext(val rootDir: Path) {

    init {
        val isPrickRoot = LibUtils.isPrickRoot(rootDir)
        if (!isPrickRoot) {
            throw RuntimeException("Directory '$rootDir' is not a prick directory.")
        }
    }

    private var isLocked: Boolean = false
        private set

    fun <T> withLock(block: () -> T): T {
        ensureLocked()
        return block()
    }

    fun withLock(block: Runnable) {
        withLock { block.run() }
    }

    @Synchronized
    private fun ensureLocked() {
        if (isLocked) {
            return
        }
        acquireFileLock()
        isLocked = true
    }

    private fun createLockFile(force: Boolean) {
        val pid = ProcessHandle.current().pid()
        val fileContent = pid.toString().toByteArray(StandardCharsets.UTF_8)
        val openOptions = if (force)
            arrayOf<OpenOption>(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        else
            arrayOf<OpenOption>(StandardOpenOption.CREATE_NEW)
        try {
            java.nio.file.Files.write(files.lock, fileContent, *openOptions)
        } catch (e: FileAlreadyExistsException) {
            throw e
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        files.lock.toFile().deleteOnExit()
    }

    private fun acquireFileLock() {
        try {
            createLockFile(false)
        } catch (e: kotlin.io.FileAlreadyExistsException) {
            acquireFileLockFileExists()
        }
    }

    private fun acquireFileLockFileExists() {
        val lockingPid = java.lang.Long.parseLong(java.nio.file.Files.readString(files.lock))
        val ownPid = ProcessHandle.current().pid()
        if (ownPid == lockingPid) {
            return
        }
        val lockingProcess = ProcessHandle.allProcesses()
                .filter { handle -> handle.pid() == lockingPid && handle.isAlive }
                .findAny()
        if (lockingProcess.isPresent) {
            throw LockedByOtherProcessException(files.lock, lockingProcess.get())
        }
        createLockFile(true)
    }

    override fun toString(): String {
        return "PrickContext(rootDir=$rootDir, isLocked=$isLocked)"
    }


    val files = Files()

    inner class Files internal constructor() {
        val prickDir: Path get() = FileNames.prickDir(rootDir)
        val head: Path get() = FileNames.head(rootDir)
        val objects: Path get() = FileNames.objects(rootDir)
        val index: Path get() = FileNames.index(rootDir)
        val lock: Path get() = FileNames.lock(rootDir)
    }

    companion object {
        fun fromWorkingPath(workingPath: Path): PrickContext {
            val prickRoot = LibUtils.findPrickRoot(workingPath)
                    ?: throw PrickException("Path '$workingPath' is not inside a prick directory.")
            return PrickContext(prickRoot)
        }
    }
}