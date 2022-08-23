package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.*;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DuplicateEventHandler implements BlockEventHandler<AbstractBlockEvent>, DisruptorBlockVisitor {
    private static Logger LOG = LoggerFactory.getLogger(DuplicateEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            AbstractBlock block = blockEvent.getBlock();
            block.accept(this);
        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
        }
    }

    @Override
    public void visit(CommitteeBlock committeeBlock) {
    }

    @Override
    public void visit(TransactionBlock transactionBlock) {
        List<Transaction> duplicates =
                transactionBlock
                        .getTransactionList()
                        .stream()
                        .collect(Collectors.groupingBy(Function.identity()))
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().size() > 1)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

        if(!duplicates.isEmpty()){
            LOG.info("Block contains duplicate transactions abort");
            transactionBlock.setStatustype(StatusType.ABORT);
        }
    }
}
