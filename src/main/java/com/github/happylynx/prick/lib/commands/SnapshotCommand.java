package com.github.happylynx.prick.lib.commands;

import com.github.happylynx.prick.lib.FileNames;
import com.github.happylynx.prick.lib.Singleton;
import com.github.happylynx.prick.lib.Utils;
import com.github.happylynx.prick.lib.model.HashId;
import com.github.happylynx.prick.lib.model.Index;
import com.github.happylynx.prick.lib.verbosity.ProgressReceiver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * inputs:
 *   - path to root
 *   - old index - files + hashes + change time
 * outputs:
 *   - snapshotId
 * continuous output:
 *   - progress
 * side effects:
 *   - updated index
 *   - new stored snapshot
 */
public class SnapshotCommand {

    private final Path snapshotRoot;
//    private final Path prickDir;

    public SnapshotCommand(Params params) {
        snapshotRoot = params.snapshotRoot;
        prickRoot = Optional.ofNullable(params.prickRoot)
                .orElseGet(() -> Utils.findPrickRoot(snapshotRoot));
        Singleton.INSTANCE.setPrickRoot(prickRoot);
        validate();
    }

    private void validate() {
        // TODO
    }

    // TODO add file level locking
    public void run(ProgressReceiver progressReceiver) {
        Utils.withLock(() -> runLocked(progressReceiver), prickRoot);
    }

    private void runLocked(ProgressReceiver progressReceiver) {
        Index newIndex = createUpdatedIndex();
        storeIndex(newIndex);
        commitIndex(newIndex);
    }

    private HashId commitIndex(Index index) {
        HashId rootTreeId = IndexToTree.convert(index);
        return createCommitFile(rootTreeId);
    }

    private HashId createCommitFile(HashId rootTree) {
        try {
            return createCommitFileUnchecked(rootTree);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HashId createCommitFileUnchecked(HashId rootTreeId) throws IOException {
        final Path commitPath = FileNames.head(prickRoot);
        final String currentCommitId = Files.readString(commitPath);
        final String commitContent = FileFormats.createCommit(rootTreeId, currentCommitId);
        final HashId commitHash = ObjectStorage.store(commitContent, prickRoot);
        Files.writeString(commitPath, commitHash.toString());
        return commitHash;
    }

    private void storeIndex(Index index) {
        try {
            Files.write(FileNames.index(prickRoot), index.toLines().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Index createUpdatedIndex() {
        final Index oldIndex = loadIndex(prickRoot);
        final Index newPartialIndex = Index.fromDisk(snapshotRoot, prickRoot, oldIndex);
        final Index newIndex = oldIndex.combineWith(newPartialIndex);
        return newIndex;

    }

    private Index loadIndex(Path prickRoot) {
        final Path indexPath = prickRoot.resolve("index");
        try (Stream<String> lines = Files.lines(indexPath)) {
            return Index.fromLines(lines);
        } catch (NoSuchFileException e) {
            return new Index(Collections.emptySortedMap(), Path.of(""));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Index file '%s' can't be read.", indexPath), e);
        }
    }

    public static class Params {
        public final Path snapshotRoot;

        /**
         * optional
         */
        public final Path prickRoot;

        public Params(Path snapshotRoot, Path prickRoot) {
            this.snapshotRoot = snapshotRoot;
            this.prickRoot = prickRoot;
        }

        public Params(Path snapshotRoot) {
            this(snapshotRoot, null);
        }
    }
}
