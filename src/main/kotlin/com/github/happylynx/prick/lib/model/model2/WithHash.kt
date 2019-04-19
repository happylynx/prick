package com.github.happylynx.prick.lib.model.model2

import com.github.happylynx.prick.lib.model.HashId
import java.time.Instant

interface WithHash {
    val hash: HashId
    val modificationInstant: Instant
}