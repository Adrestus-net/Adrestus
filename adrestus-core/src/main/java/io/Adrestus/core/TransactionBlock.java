package io.Adrestus.core;

public class TransactionBlock extends AbstractBlock {
    private int tr;

    public TransactionBlock(int tr) {
        super(tr);
        this.tr = tr;
    }
}
