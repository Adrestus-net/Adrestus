package io.Adrestus.protocol;

import io.Adrestus.config.TransactionConfigOptions;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedReceiptSemaphore;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.util.SerializationUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.AsyncTcpSocket;
import io.activej.net.socket.tcp.AsyncTcpSocketNio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;

public class ReceiptTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(ReceiptTask.class);

    private final SerializationUtil<Receipt> recep;

    private volatile boolean runner;
    private Eventloop eventloop;
    private AsyncTcpSocket socket;

    public ReceiptTask() {
        this.eventloop = Eventloop.create().withCurrentThread();
        this.recep = new SerializationUtil<Receipt>(Receipt.class);
        this.runner = false;
    }

    @Override
    public void execute() {
        while (!runner) {
            try {
                CachedReceiptSemaphore.getInstance().getSemaphore().acquire();
                if (!CachedLatestBlocks.getInstance().getTransactionBlock().getOutbound().getMap_receipts().isEmpty()) {
                    CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).values().forEach(ip -> {
                        eventloop.connect(new InetSocketAddress(ip, TransactionConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
                            if (e == null) {
                                // System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                                try {
                                    socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                                } catch (IOException ioException) {
                                    throw new RuntimeException(ioException);
                                }

                                CachedLatestBlocks.getInstance().getTransactionBlock().getOutbound().getMap_receipts().get(0).entrySet().forEach(val -> {
                                    val.getValue().stream().forEach(receipt -> {
                                        TransactionBlock transactionBlock = CachedLatestBlocks.getInstance().getTransactionBlock();
                                        if (!transactionBlock.getHash().equals("hash")) {
                                            receipt.setReceiptBlock(new Receipt.ReceiptBlock(transactionBlock.getHash(), transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot()));
                                            byte transaction_hash[] = recep.encode(receipt, 1024);
                                            ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
                                            sizeBuf.writeVarInt(transaction_hash.length);
                                            ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(transaction_hash));
                                            socket.write(appendedBuf);
                                        }
                                    });
                                });
                                socket.close();
                                socket = null;

                            } else {
                                LOG.info("Could not connect to server, make sure it is started: %s%n", e);
                            }
                        });
                    });
                    eventloop.run();
                }
                CachedReceiptSemaphore.getInstance().getSemaphore().release();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
