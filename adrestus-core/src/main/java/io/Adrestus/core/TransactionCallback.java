package io.Adrestus.core;

import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;

public class TransactionCallback extends Callback {
    private ArrayList<String> messages;

    public TransactionCallback() {
        messages = new ArrayList<>();
    }

    @Override
    public void call(String value) {
        messages.add(value);
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }

    @Serialize
    public ArrayList<String> getMessages() {
        return messages;
    }
}
