//package com.github.happylynx.prick.lib;
//
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class UtilsTest {
//
//    private static long NONEXISTING_PID = -1;
//
//    @Test
//    void lockCreatedIfNonexistent() throws IOException {
//        final Path tmpDir = createTmpDir();
//
//        Utils.lock(tmpDir);
//
//        assertLocked(tmpDir);
//    }
//
//    @Test
//    void lockCreatedIfExistentDead() throws IOException {
//        final Path tmpDir = createTmpDir();
//        createLockFile(tmpDir, NONEXISTING_PID);
//
//        Utils.lock(tmpDir);
//
//        assertLocked(tmpDir);
//    }
//
//    private void assertLocked(Path prickRoot) throws IOException {
//        final String lockContent = Files.readString(FileNames.lock(prickRoot));
//        final long lockPid = Long.parseLong(lockContent.trim());
//        assertEquals(ProcessHandle.current().pid(), lockPid);
//    }
//
//    @Test
//    void lockFailsIfLockedByOther() throws IOException {
//        final Path tmpDir = createTmpDir();
//        final long currentPid = ProcessHandle.current().pid();
//        final long otherProcessPid = ProcessHandle.allProcesses()
//                .filter(handle -> handle.pid() != currentPid && handle.isAlive())
//                .findAny()
//                .get()
//                .pid();
//        createLockFile(tmpDir, otherProcessPid);
//
//        assertThrows(LockedByOtherProcessException.class, () -> Utils.lock(tmpDir));
//    }
//
//    @Test
//    void lockPassesIfAlreadyLockedByThisProcess() throws IOException {
//        final Path tmpDir = createTmpDir();
//        createLockFile(tmpDir, ProcessHandle.current().pid());
//
//        Utils.lock(tmpDir);
//
//        assertLocked(tmpDir);
//    }
//
//    private void createLockFile(Path directory, long pid) throws IOException {
//        Files.write(FileNames.lock(directory), String.valueOf(pid).getBytes());
//    }
//
//    private Path createTmpDir() {
//        final Path tempDirectory;
//        try {
//            tempDirectory = Files.createTempDirectory(this.getClass().getCanonicalName());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        tempDirectory.toFile().deleteOnExit();
//        return tempDirectory;
//    }
//
//}