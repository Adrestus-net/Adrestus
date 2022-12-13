package io.Adrestus.rpc;

import java.util.List;

public interface IService<T> {
    List<T> download(String hash) throws Exception;

    T getBlock(String hash) throws Exception;
}
