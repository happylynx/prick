package com.github.happylynx.prick.lib.commands;

import com.github.happylynx.prick.lib.model.HashId;
import com.github.happylynx.prick.lib.model.Index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
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

    private final Path relativeSnapshotRoot;
    private final PrickContext ctx;

    public SnapshotCommand(PrickContext ctx, Path relativeSnapshotRoot) {
        this.relativeSnapshotRoot = relativeSnapshotRoot;
        this.ctx = ctx;
        validate();
    }

    private void validate() {
        // TODO
    }

    // TODO add file level locking
    public void run() {
        ctx.withLock(this::runLocked);
    }

    private void runLocked() {
        final Index newIndex = createUpdatedIndex();
        storeIndex(newIndex);
        commitIndex(newIndex);
    }

    private HashId commitIndex(Index index) {
        HashId rootTreeId = IndexToTree.writeTree(index, ctx);
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
        final Path headFile = ctx.getFiles().getHead();
        headFile.toFile().createNewFile();
        final String currentCommitId = Files.readString(headFile);
        final String commitContent = FileFormats.INSTANCE.createCommit(rootTreeId, currentCommitId);
        final HashId commitHash = ObjectStorage.INSTANCE.store(commitContent, ctx);
        Files.writeString(headFile, commitHash.toString());
        return commitHash;
    }

    private void storeIndex(Index index) {
        try {
            Files.write(ctx.getFiles().getIndex(), index.toLines().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Index createUpdatedIndex() {
        final Index oldIndex = loadIndex();
        final Index newPartialIndex = Index.fromDisk(ctx.getRootDir().resolve(relativeSnapshotRoot), ctx, oldIndex);
        final Index newIndex = oldIndex.combineWith(newPartialIndex);
        return newIndex;

    }

    private Index loadIndex() {
        // TODO use FileFormats
        final Path indexPath = ctx.getFiles().getIndex();
        try (Stream<String> lines = Files.lines(indexPath)) {
            return Index.fromLines(lines);
        } catch (NoSuchFileException e) {
            return Index.empty();
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
