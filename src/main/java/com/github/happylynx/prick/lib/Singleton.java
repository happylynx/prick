package com.github.happylynx.prick.lib;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public enum Singleton {

    INSTANCE;

    public Set<UUID> lockers = Collections.synchronizedSet(new HashSet<>(1));
    public AtomicBoolean fileLocked = new AtomicBoolean(false);

    public Path prickRoot;

    public void setPrickRoot(Path prickRoot) {
        if (this.prickRoot != null && !this.prickRoot.equals(prickRoot)) {
            throw new RuntimeException(String.format(
                    "Setting of prick root to '%s' failed. Prick root has already been set to '%s'.",
                    prickRoot,
                    this.prickRoot));
        }
        this.prickRoot = prickRoot;
    }
}
