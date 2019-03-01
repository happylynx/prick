package com.github.happylynx.prick.lib;

import java.nio.file.Path;

public class LockByOtherProcessException extends RuntimeException {
    public LockByOtherProcessException(Path lockFile, ProcessHandle processHandle) {
        super(String.format("Prick directory '%s' is already locked by proceess %d.",
                lockFile.getParent().toAbsolutePath(),
                processHandle.pid()));
    }
}
