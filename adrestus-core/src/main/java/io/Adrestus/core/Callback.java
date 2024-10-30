package io.Adrestus.core;

import io.activej.serializer.annotations.SerializeClass;

import java.io.Serializable;

@SerializeClass(subclasses = {TransactionCallback.class})
public abstract class Callback extends Object implements Serializable {
    public abstract void call(String value);
}
