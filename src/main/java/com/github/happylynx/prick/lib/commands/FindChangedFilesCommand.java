package com.github.happylynx.prick.lib.commands;

import com.github.happylynx.prick.lib.verbosity.ProgressReceiver;
import com.github.happylynx.prick.lib.verbosity.Registration;

import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FindChangedFilesCommand implements Command {

    private final Path root;
    private final Path timestampsFile;

    public FindChangedFilesCommand(Path root, Path timestampsFile) {
        this.root = root;
        this.timestampsFile = timestampsFile;
    }

    @Override
    public void run() {
//        Files.walk(root, FileVisitOption.FOLLOW_LINKS)
//                .map(fsEntry -> fsEntry.relativize())
    }

    @Override
    public Map<ProgressReceiver, Registration> getProgressReceivers() {
        return null;
    }
}
