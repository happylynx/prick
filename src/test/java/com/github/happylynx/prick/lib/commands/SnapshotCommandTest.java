package com.github.happylynx.prick.lib.commands;

import com.github.happylynx.prick.lib.FileNames;
import com.github.happylynx.prick.lib.verbosity.ProgressReceiver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class SnapshotCommandTest {

    private Set<Path> dirsToDelete = new HashSet<>();

    private Path createTmpPrickDir() {
        final Path directory;
        try {
            directory = Files.createTempDirectory(this.getClass().getName());
            Files.createDirectory(FileNames.prickDir(directory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        directory.toFile().deleteOnExit();
        dirsToDelete.add(directory);
        return directory;
    }

    @AfterEach
    private void deleteDirs() {
        dirsToDelete.forEach(SnapshotCommandTest::deleteRecursively);
    }

    private static void deleteRecursively(Path root) {
        // todo fix links
        try {
            Files.walk(root)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testEmptyDirectory(@Mock ProgressReceiver progressReceiver) {
        final Path prickDir = createTmpPrickDir();
        SnapshotCommand.Params params = new SnapshotCommand.Params(prickDir);
        final SnapshotCommand command = new SnapshotCommand(params);
        command.run(progressReceiver);
    }

}