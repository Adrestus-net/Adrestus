package io.Adrestus.core;

import java.io.Serializable;

public interface TransactionCallback extends Serializable {

    void call(String value);
}
