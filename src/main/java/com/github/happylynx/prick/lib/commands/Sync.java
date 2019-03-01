package com.github.happylynx.prick.lib.commands;

import com.github.happylynx.prick.lib.verbosity.ProgressReceiver;
import com.github.happylynx.prick.lib.verbosity.Registration;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Sync implements Command {

    private Map<ProgressReceiver, Registration> progressReceivers;

    public Sync(List<Path> syncRoots, ConflictResolver conflictResolver) {

    }

    public void run() {
//        new SnapshotCommand().run();
    }

    @Override
    public Map<ProgressReceiver, Registration> getProgressReceivers() {
        return progressReceivers;
    }

    public interface ConflictResolver {
        List<ConflictResolution> resolve(List<Conflict> conflicts);
    }

    public static class Conflict {

    }

    public static class ConflictResolution {

    }
}
