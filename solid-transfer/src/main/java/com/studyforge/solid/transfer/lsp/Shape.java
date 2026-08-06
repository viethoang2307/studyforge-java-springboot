package com.studyforge.solid.transfer.lsp;

/** Honest shared contract: immutable shapes promise only their area. */
public sealed interface Shape permits ImmutableRectangle, ImmutableSquare {
    int area();
}
