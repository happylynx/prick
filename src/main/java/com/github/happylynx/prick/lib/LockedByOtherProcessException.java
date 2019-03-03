package com.github.happylynx.prick.lib;

import java.nio.file.Path;

public class LockedByOtherProcessException extends RuntimeException {
    public LockedByOtherProcessException(Path lockFile, ProcessHandle processHandle) {
        super(String.format("Prick directory '%s' is already withLock by proceess %d.",
                lockFile.getParent().toAbsolutePath(),
                processHandle.pid()));
    }
}
