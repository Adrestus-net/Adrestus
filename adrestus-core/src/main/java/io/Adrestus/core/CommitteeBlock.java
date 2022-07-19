package io.Adrestus.core;

public class CommitteeBlock extends AbstractBlock implements BlockFactory {
    public CommitteeBlock(int var) {
        super(var);
    }

    @Override
    public void accept(BlockForge visitor) {
        visitor.forgeCommitteBlock(this);
    }
}
