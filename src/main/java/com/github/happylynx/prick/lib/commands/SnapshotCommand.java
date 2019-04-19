package com.github.happylynx.prick.lib.commands;

import com.github.happylynx.prick.lib.model.HashId;
import com.github.happylynx.prick.lib.model.Index;
import com.github.happylynx.prick.lib.model.model2.IndexToTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        // TODO rework
        // 1. load cache (only files and symlinks) - path, mod date, hash, type
        // 2. create index
        // 3. update cache
        // 4. write tree
        // 5. write commit
        // 6. update head

        final Index newIndex = createUpdatedIndex();
        newIndex.toFile(ctx.getFiles().getIndex());
        commitIndex(newIndex);
    }

    private HashId commitIndex(Index index) {
        HashId rootTreeId = IndexToTree.Companion.run(index, ctx);
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

    private Index createUpdatedIndex() {
        final Index oldIndex = loadIndex();
        final Index newPartialIndex = Index.Companion.fromDisk(ctx.getRootDir().resolve(relativeSnapshotRoot), ctx, oldIndex);
        final Index newIndex = oldIndex.combineWith(newPartialIndex);
        return newIndex;

    }

    private Index loadIndex() {
        final Path indexPath = ctx.getFiles().getIndex();
        return Index.Companion.fromFile(indexPath);
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
