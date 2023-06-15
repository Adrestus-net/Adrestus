package io.Adrestus.api;

import io.Adrestus.config.APIConfiguration;
import io.reactivex.rxjava3.subscribers.DefaultSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class MessageListener extends DefaultSubscriber<String> {
    private static Logger LOG = LoggerFactory.getLogger(MessageListener.class);
    private ArrayList<String> consume_list;
    private int size;

    public MessageListener() {}

    @Override
    protected void onStart() {
        request(size);
        consume_list=new ArrayList<>(size);
    }

    @Override
    public void onNext(String msg) {
        LOG.info("consuming {}"+msg);
        this.consume_list.add(msg);
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("error received"+throwable);
    }

    @Override
    public void onComplete() {
        if(this.consume_list.stream().anyMatch(val->val.equals(APIConfiguration.MSG_FAILED)))
            LOG.info("consumer finished");//UpDateUI
    }


    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
