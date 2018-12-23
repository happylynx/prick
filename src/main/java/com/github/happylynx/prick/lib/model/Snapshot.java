package com.github.happylynx.prick.lib.model;

import java.time.ZonedDateTime;
import java.util.List;

public class Snapshot {
    private List<HashId> parents;
    private HashId tree;
    private ZonedDateTime creationDate;
}
