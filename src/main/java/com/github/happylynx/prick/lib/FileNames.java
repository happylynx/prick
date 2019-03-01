package com.github.happylynx.prick.lib;

import java.nio.file.Path;

public class FileNames {

    private FileNames() {}

    public static Path lock(Path prickRoot) {
        return prickDir(prickRoot).resolve("lock");
    }

    public static Path index(Path prickRoot) {
        return prickDir(prickRoot).resolve("index");
    }

    public static Path objects(Path prickRoot) {
        return prickDir(prickRoot).resolve("objects");
    }

    public static Path head(Path prickRoot) {
        return prickDir(prickRoot).resolve("head");
    }


    public static Path prickDir(Path prickRoot) {
        return prickRoot.resolve(".prick");
    }
}
