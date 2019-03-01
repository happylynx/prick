package com.github.happylynx.prick.lib.walking;

import com.github.happylynx.prick.lib.Utils;
import com.github.happylynx.prick.lib.model.FsEntryType;
import com.github.happylynx.prick.lib.model.HashId;

import java.nio.file.Path;
import java.util.stream.Stream;

public enum FsNonDirEntryType implements FsEntryType {
    FILE("f") {
        public HashId hash(Path file) {
            return Utils.hashFile(file);
        }
    },
    SYMLINK("l") {
        public HashId hash(Path symlink) {
            return Utils.hashSymlink(symlink);
        }
    };

    private final String code;

    FsNonDirEntryType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    public HashId hash(Path path) {
        throw new UnsupportedOperationException();
    }

    public static FsNonDirEntryType parse(String code) {
        return Stream.of(FsNonDirEntryType.values())
                .filter(type -> type.code.equals(code))
                .findAny()
                .orElseThrow();
    }
}
