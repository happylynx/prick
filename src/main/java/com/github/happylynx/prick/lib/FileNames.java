package com.github.happylynx.prick.lib;

import java.nio.file.Path;

public class FileNames {

    private FileNames() {}

    public static Path lock(Path prickRoot) {
        return prickDir(prickRoot).resolve("lock");
    }

    /**
     * Copy of the last snapshot
     *
     * <p>It may not exist</p>
     */
    public static Path index(Path prickRoot) {
        return prickDir(prickRoot).resolve("index");
    }

    /**
     * Object storage. A directory.
     *
     * <p>It may not exist</p>
     */
    public static Path objects(Path prickRoot) {
        return prickDir(prickRoot).resolve("objects");
    }

    /**
     * Link to the latest snapshot
     *
     * <p>It may not exist</p>
     */
    public static Path head(Path prickRoot) {
        return prickDir(prickRoot).resolve("head");
    }


    public static Path prickDir(Path prickRoot) {
        return prickRoot.resolve(".prick");
    }
}
