package com.github.happylynx.prick.lib.model;

import java.time.Instant;
import java.util.List;

public class Commit {
    private final HashId tree;
    private final Instant created;
    private final List<HashId> parents;

    public Commit(HashId tree, Instant created, List<HashId> parents) {
        this.tree = tree;
        this.created = created;
        this.parents = parents;
    }
}
