package io.Adrestus.rpc;

import io.Adrestus.core.AbstractBlock;

import java.util.List;

public interface IService {
    List<AbstractBlock> download(String hash) throws Exception;
}
