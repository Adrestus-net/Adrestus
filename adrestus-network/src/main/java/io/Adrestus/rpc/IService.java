package io.Adrestus.rpc;

import java.util.ArrayList;
import java.util.List;

public interface IService<T> {
    List<T> download(String hash) throws Exception;

    List<T> migrateBlock(ArrayList<String> list_hash) throws Exception;
}
