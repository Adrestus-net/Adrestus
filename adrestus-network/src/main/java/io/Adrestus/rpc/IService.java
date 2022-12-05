package io.Adrestus.rpc;

import java.util.List;

public interface IService<T> {
    List<T> download(String hash) throws Exception;
}
