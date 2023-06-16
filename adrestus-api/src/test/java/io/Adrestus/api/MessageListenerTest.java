package io.Adrestus.api;

import io.Adrestus.config.APIConfiguration;
import org.junit.jupiter.api.Test;

public class MessageListenerTest {


    @Test
    public void test() {
        MessageListener messageListener = new MessageListener();
        messageListener.setSize(3);


        messageListener.onStart();
        messageListener.onNext("1");
        messageListener.onNext("2");
        messageListener.onNext(APIConfiguration.MSG_FAILED);

        messageListener.onComplete();
    }

}
