package com.github.happylynx.prick.lib;

import com.github.happylynx.prick.lib.model.HashId;
import com.github.happylynx.prick.lib.walking.DirectoryWalker;
import com.github.happylynx.prick.lib.walking.FsEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Utils {

    private static final int MEGA = 1024 * 1024;

    private Utils() {}

    public static HashId hashFile(final Path path) {
        return hashToString(hashFileToBytes(path));
    }

    public static HashId hashSymlink(final Path symlink) {
        return hashToString(createMessageDigest().digest(symlink.toString().getBytes()));
    }

    private static byte[] hashFileToBytes(final Path path) {
        try (SeekableByteChannel byteChannel = Files.newByteChannel(path, LinkOption.NOFOLLOW_LINKS, StandardOpenOption.READ)) {
            return hashByteChannelToBytes(byteChannel);
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static byte[] hashByteChannelToBytes(ByteChannel byteChannel) throws NoSuchAlgorithmException, IOException {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(2 * MEGA);
        final MessageDigest messageDigest = createMessageDigest();
        int read = byteChannel.read(byteBuffer);
        while (read > 0) {
            byteBuffer.flip();
            messageDigest.update(byteBuffer);
            byteBuffer.clear();
            read = byteChannel.read(byteBuffer);
        }
        return messageDigest.digest();
    }

    private static MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static BytesHash hashString(String content) {
        final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return new BytesHash(bytes);
    }

    public static HashId hashBytes(byte[] bytes) {
        return hashToString(createMessageDigest().digest(bytes));
    }

    static HashId hashToString(byte[] binaryHash) {
        final StringBuilder builder = new StringBuilder(binaryHash.length * 2);
        for (byte hashByte : binaryHash) {
            builder.append(Integer.toHexString(0xff & hashByte));
        }
        return new HashId(builder.toString());
    }

    public static Stream<FsEntry> dirStream(Path root) {
        Spliterator<FsEntry> dirSpliterator = Spliterators.spliteratorUnknownSize(dirIterator(root), Spliterator.ORDERED);
        return StreamSupport.stream(dirSpliterator, false);

    }

    static void lock(Path prickRoot) {
        synchronized (Singleton.INSTANCE.fileLocked) {
            final Path lockFile = FileNames.lock(prickRoot);
            try {
                createLockFile(lockFile, false);
            } catch (FileAlreadyExistsException e) {
                lockLockfileExists(lockFile);
            }
            Singleton.INSTANCE.fileLocked.set(true);
        }
    }

    private static void createLockFile(Path lockFile, boolean force) throws FileAlreadyExistsException {
        final long pid = ProcessHandle.current().pid();
        final byte[] fileContent = String.valueOf(pid).getBytes(StandardCharsets.UTF_8);
        final OpenOption[] openOptions = force
                ? new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING }
                : new OpenOption[] { StandardOpenOption.CREATE_NEW };
        try {
            Files.write(lockFile, fileContent, openOptions);
        } catch (FileAlreadyExistsException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void lockLockfileExists(Path lockFile) {
        final long lockingPid;
        try {
            lockingPid = Long.parseLong(Files.readString(lockFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final long ownPid = ProcessHandle.current().pid();
        if (ownPid == lockingPid) {
            return;
        }
        final Optional<ProcessHandle> lockingProcess = ProcessHandle.allProcesses()
                .filter(handle -> handle.pid() == lockingPid && handle.isAlive())
                .findAny();
        if (lockingProcess.isPresent()) {
            throw new LockByOtherProcessException(lockFile, lockingProcess.get());
        }
        try {
            createLockFile(lockFile, true);
        } catch (FileAlreadyExistsException e) {
            throw new RuntimeException("This should never happen.", e);
        }
    }

    public static void withLock(Runnable task, Path prickRoot) {
        final UUID lockId = UUID.randomUUID();
        try {
            Singleton.INSTANCE.lockers.add(lockId);
            if (!Singleton.INSTANCE.fileLocked.get()) {
                lock(prickRoot);
            }
            task.run();
        }
        finally {
            Singleton.INSTANCE.lockers.remove(lockId);
            freeLock(prickRoot);
        }
    }

    private static void freeLock(Path prickRoot) {
        synchronized (Singleton.INSTANCE.fileLocked) {
            if (!Singleton.INSTANCE.lockers.isEmpty()) {
                return;
            }
            try {
                Files.delete(FileNames.lock(prickRoot));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Singleton.INSTANCE.fileLocked.set(false);
        }
    }

    private static Iterator<FsEntry> dirIterator(Path root) {
        final DirectoryWalker directoryWalker = new DirectoryWalker(root);
        return new Iterator<>() {
            private FsEntry nextElement = directoryWalker.getNext();

            @Override
            public boolean hasNext() {
                return nextElement != null;
            }

            @Override
            public FsEntry next() {
                if (nextElement == null) {
                    throw new NoSuchElementException();
                }
                final FsEntry oldNextElement = nextElement;
                nextElement = directoryWalker.getNext();
                return oldNextElement;
            }
        };
    }

    /**
     * @return {@code null} if not found, superdirectory that contains `.prick` subdirectory.
     */
    public static Path findPrickRoot(Path fsItem) {
        Path currentPath = fsItem.normalize();
        while(true) {
            boolean found = isPrickRoot(currentPath);
            if (found) {
                return currentPath;
            }
            currentPath = currentPath.getParent();
            if (currentPath == null) {
                return null;
            }
        }
    }

    private static boolean isPrickRoot(Path path) {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
                && Files.isDirectory(FileNames.prickDir(path));
    }

    public static class BytesHash {
        private final byte[] bytes;
        private final HashId hash;

        public BytesHash(byte[] bytes) {
            this.bytes = bytes;
            hash = hashBytes(bytes);
        }

        public byte[] getBytes() {
            return bytes;
        }

        public HashId getHash() {
            return hash;
        }
    }
}
