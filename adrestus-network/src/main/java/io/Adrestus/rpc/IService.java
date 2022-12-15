package io.Adrestus.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface IService<T> {
    List<T> download(String hash) throws Exception;

    List<T> migrateBlock(ArrayList<String> list_hash) throws Exception;
}
