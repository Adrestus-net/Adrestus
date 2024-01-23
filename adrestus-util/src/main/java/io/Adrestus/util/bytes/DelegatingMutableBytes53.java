package io.Adrestus.util.bytes;

import static io.Adrestus.util.bytes.Checks.checkArgument;

public class DelegatingMutableBytes53 extends DelegatingMutableBytes implements MutableBytes53 {

    private DelegatingMutableBytes53(MutableBytes delegate) {
        super(delegate);
    }

    static MutableBytes53 delegateTo(MutableBytes value) {
        checkArgument(value.size() == SIZE, "Expected %s bytes but got %s", SIZE, value.size());
        return new DelegatingMutableBytes53(value);
    }

    @Override
    public Bytes53 copy() {
        return Bytes53.wrap(delegate.toArray());
    }

    @Override
    public MutableBytes53 mutableCopy() {
        return MutableBytes53.wrap(delegate.toArray());
    }
}
