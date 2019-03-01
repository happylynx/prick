package com.github.happylynx.prick.lib.model;

public enum FsDirType implements FsEntryType {
    DIRECTORY;

    @Override
    public String getCode() {
        return "d";
    }
}
