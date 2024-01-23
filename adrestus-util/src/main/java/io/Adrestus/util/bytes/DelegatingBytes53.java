package io.Adrestus.util.bytes;

public class DelegatingBytes53 extends DelegatingBytes implements Bytes53 {

    protected DelegatingBytes53(Bytes delegate) {
        super(delegate);
    }

    @Override
    public int size() {
        return Bytes53.SIZE;
    }

    @Override
    public Bytes53 copy() {
        return Bytes53.wrap(toArray());
    }

    @Override
    public MutableBytes53 mutableCopy() {
        return MutableBytes53.wrap(toArray());
    }

}
