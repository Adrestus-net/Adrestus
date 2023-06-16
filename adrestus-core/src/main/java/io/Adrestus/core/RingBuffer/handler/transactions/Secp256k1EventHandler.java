package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;

public class Secp256k1EventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(Secp256k1EventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();

        if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
            return;

        if (transaction.getXAxis().equals(new BigInteger("0")) || transaction.getYAxis().equals(new BigInteger("0")))
            return;

        try {
            ECPoint point = new ECPoint(transaction.getXAxis(), transaction.getYAxis());

            KeyFactory kfBc = KeyFactory.getInstance(AdrestusConfiguration.SIGN_ALGORITHM, AdrestusConfiguration.SIGN_PROVIDER);
            AlgorithmParameters parameters = AlgorithmParameters.getInstance(AdrestusConfiguration.SIGN_ALGORITHM, AdrestusConfiguration.SIGN_PROVIDER);
            parameters.init(new ECGenParameterSpec(AdrestusConfiguration.SIGN_CURVE));
            ECParameterSpec ecParamSpec = parameters.getParameterSpec(ECParameterSpec.class);
            PublicKey pubKey = kfBc.generatePublic(new ECPublicKeySpec(point, ecParamSpec));
            BCECPublicKey publicKeys = (BCECPublicKey) pubKey;
        } catch (Exception e) {
            LOG.info("Transaction abort Secp256k1 curve is not valid with theese X,Y inputs");
            transaction.setStatus(StatusType.ABORT);
        }
    }

}
