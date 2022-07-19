package io.Adrestus.core;

public class TransactionBlock extends AbstractBlock implements BlockFactory {
    private int tr;

    public TransactionBlock(int tr) {
        super(tr);
        this.tr = tr;
    }

    @Override
    public void accept(BlockForge visitor) {
        visitor.forgeTransactionBlock(this);
    }
}
