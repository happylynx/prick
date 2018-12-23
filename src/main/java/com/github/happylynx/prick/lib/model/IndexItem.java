package com.github.happylynx.prick.lib.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class IndexItem {
    private Path path;
    private String contentHash;
    private LocalDateTime changeTime;
}
