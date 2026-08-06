package com.studyforge.transfer;

import java.time.Instant;

@FunctionalInterface
public interface Clock {
    Instant now();
}
