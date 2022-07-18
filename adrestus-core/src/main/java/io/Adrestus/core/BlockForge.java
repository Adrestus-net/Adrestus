package io.Adrestus.core;

public interface BlockForge<T extends AbstractBlock,R extends AbstractBlock> {

  R forgeBlock(T t);
}
