package io.Adrestus.protocol;

import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.SerializationUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.AsyncTcpSocket;
import io.activej.net.socket.tcp.AsyncTcpSocketNio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;

public class ReceiptTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(ReceiptTask.class);
    private static final int CONNECT_TIMER_DELAY_TIMEOUT = 4000;
    private final SerializationUtil<Receipt> recep;

    private volatile boolean runner;
    private Eventloop eventloop;
    private AsyncTcpSocket socket;

    private TransactionBlock transactionBlockPrev;

    public ReceiptTask() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        this.eventloop = Eventloop.create().withCurrentThread();
        this.recep = new SerializationUtil<Receipt>(Receipt.class, list);
        this.runner = false;
        this.transactionBlockPrev = null;
    }

    @Override
    public void execute() {
        while (!runner) {
            try {
                if (transactionBlockPrev != CachedLatestBlocks.getInstance().getTransactionBlock()) {
                    TransactionBlock transactionBlock = CachedLatestBlocks.getInstance().getTransactionBlock();
                    if (!transactionBlock.getOutbound().getMap_receipts().isEmpty()) {
                        LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> outer_receipts = transactionBlock.getOutbound().getMap_receipts();
                        for (Integer key : outer_receipts.keySet()) {
                            if (key != CachedZoneIndex.getInstance().getZoneIndex()) {
                                CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(key).values().forEach(ip -> {
                                    //System.out.println("key :"+key+" with ip:"+ip);
                                    eventloop.connect(new InetSocketAddress(ip, SocketConfigOptions.RECEIPT_PORT), CONNECT_TIMER_DELAY_TIMEOUT, (socketChannel, e) -> {
                                        if (e == null) {
                                            // System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                                            try {
                                                socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                                            } catch (IOException ioException) {
                                                throw new RuntimeException(ioException);
                                            }

                                            transactionBlock.getOutbound().getMap_receipts().get(key).entrySet().forEach(val -> {
                                                val.getValue().stream().forEach(receipt -> {
                                                    receipt.setReceiptBlock(new Receipt.ReceiptBlock(transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot()));
                                                    byte receipt_byte[] = recep.encode(receipt, 1024);
                                                    ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
                                                    sizeBuf.writeVarInt(receipt_byte.length);
                                                    ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(receipt_byte));
                                                    socket.write(appendedBuf);
                                                });
                                            });
                                            socket.close();
                                            socket = null;
                                            try {
                                                transactionBlockPrev = (TransactionBlock) transactionBlock.clone();
                                            } catch (CloneNotSupportedException ex) {
                                                throw new RuntimeException(ex);
                                            }

                                        }
                                    });
                                });
                                eventloop.run();
                            }
                        }
                    }
                }
                Thread.sleep(500);
            } catch (Exception e) {
                LOG.info("Receipt Exception caught", e.toString());
            }
        }
    }

    @Override
    public void close() {
        runner = true;
        if (socket != null) {
            socket.close();
            socket = null;
        }
        if (eventloop != null) {
            eventloop.breakEventloop();
            eventloop = null;
        }
    }
}
