package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.*;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

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
        List<StakingData> duplicates =
                committeeBlock
                        .getStakingMap()
                        .keySet()
                        .stream()
                        .collect(Collectors.groupingBy(Function.identity()))
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().size() > 1)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

        List<KademliaData> duplicate_address = committeeBlock
                .getStakingMap()
                .values()
                .stream()
                .filter(e -> Collections.frequency(committeeBlock.getStakingMap().values().stream().collect(Collectors.toList()), e) > 1)
                .collect(Collectors.toList());

        List<KademliaData> duplicate_kademliaData = committeeBlock
                .getStakingMap()
                .values()
                .stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(e -> e.getAddressData().getAddress()))), ArrayList::new));

        if (!duplicates.isEmpty() || !duplicate_address.isEmpty()) {
            LOG.info("Committee Block contains duplicate stakes of users");
            committeeBlock.setStatustype(StatusType.ABORT);
            return;
        }

        if (duplicate_kademliaData.size() != committeeBlock.getStakingMap().values().size()) {
            LOG.info("Committee Block contains duplicate address of users");
            committeeBlock.setStatustype(StatusType.ABORT);
            return;
        }

    }

    @SneakyThrows
    @Override
    public void visit(TransactionBlock transactionBlock) {
        TransactionBlock transactionBlockcloned = (TransactionBlock) transactionBlock.clone();
        List<Transaction> duplicates =
                transactionBlockcloned
                        .getTransactionList()
                        .stream()
                        .collect(Collectors.groupingBy(Function.identity()))
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().size() > 1)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

        if (!duplicates.isEmpty()) {
            LOG.info("Block contains duplicate transactions abort");
            transactionBlock.setStatustype(StatusType.ABORT);
        }
    }
}
