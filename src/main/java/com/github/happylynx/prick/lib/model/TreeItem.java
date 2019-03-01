package com.github.happylynx.prick.lib.model;

import java.nio.file.Path;

public interface TreeItem {
    Path getPath();
    FsEntryType getType();
    HashId getHash();
}
