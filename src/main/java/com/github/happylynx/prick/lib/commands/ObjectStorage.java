package com.github.happylynx.prick.lib.commands;

import com.github.happylynx.prick.lib.FileNames;
import com.github.happylynx.prick.lib.Utils;
import com.github.happylynx.prick.lib.model.HashId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ObjectStorage {

    private ObjectStorage() {}

    public static HashId store(String content, Path prickRoot) {
        return store(content.getBytes(StandardCharsets.UTF_8), prickRoot);
    }

    private static HashId store(byte[] bytes, Path prickRoot) {
        final HashId hash = Utils.hashBytes(bytes);
        final Path destination = FileNames.objects(prickRoot)
                .resolve(hash.toString().substring(0, 2))
                .resolve(hash.toString());
        if (!Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.write(destination, bytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return hash;
    }
}
