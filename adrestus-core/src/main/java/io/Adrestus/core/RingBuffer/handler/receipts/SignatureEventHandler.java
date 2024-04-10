package io.Adrestus.core.RingBuffer.handler.receipts;

import io.Adrestus.core.ReceiptBlock;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.elliptic.ECDSASign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class SignatureEventHandler implements ReceiptEventHandler<ReceiptBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(SignatureEventHandler.class);
    private final ECDSASign ecdsaSign;

    public SignatureEventHandler() {
        this.ecdsaSign = new ECDSASign();
    }

    @Override
    public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws InterruptedException {
        ReceiptBlock receiptBlock = receiptBlockEvent.getReceiptBlock();
        boolean res;
        if (!receiptBlock.getTransaction().getXAxis().toString().equals("0") && !receiptBlock.getTransaction().getYAxis().toString().equals("0")) {
            BigInteger publicKeyValue = ecdsaSign.recoverPublicKeyValue(receiptBlock.getTransaction().getXAxis(), receiptBlock.getTransaction().getYAxis());
            res = ecdsaSign.secp256Verify(HashUtil.sha256(receiptBlock.getTransaction().getHash().getBytes(StandardCharsets.UTF_8)), receiptBlock.getTransaction().getFrom(), publicKeyValue, receiptBlock.getTransaction().getSignature());
        } else {
            res = ecdsaSign.secp256Verify(Hex.decode(receiptBlock.getTransaction().getHash()), receiptBlock.getTransaction().getFrom(), receiptBlock.getTransaction().getSignature());
        }
        if (!res) {
            LOG.info("Signatures are not equal abort");
            receiptBlockEvent.getReceiptBlock().setStatusType(StatusType.ABORT);
            return;
        }
    }
}
