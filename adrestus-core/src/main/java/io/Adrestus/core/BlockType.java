package io.Adrestus.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Getter
public enum BlockType {

    GENESIS(Genesis::new),
    REGULAR(RegularBlock::getInstance);

    private final Supplier<BlockForge> constructor;
}
