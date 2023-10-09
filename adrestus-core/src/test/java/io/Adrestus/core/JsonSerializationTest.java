package io.Adrestus.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSerializationTest {

    @Test
    public void test() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Transaction transaction=new RegularTransaction("hash1",TransactionType.REGULAR,StatusType.PENDING,0,0,"","","",0,0,0,new ECDSASignatureData());
        Transaction transaction2=new RegularTransaction();
        transaction2.setHash("hash2");
        transaction2.setType(TransactionType.REWARDS);
        String jsonDataString = mapper.writeValueAsString(transaction);
        String jsonDataString2 = mapper.writeValueAsString(transaction2);

        assertEquals("{\"transactiontype\":\"RegularTransaction\",\"type\":\"REGULAR\",\"status\":\"PENDING\",\"timestamp\":\"\",\"hash\":\"hash1\",\"nonce\":0,\"blockNumber\":0,\"from\":\"\",\"to\":\"\",\"zoneFrom\":0,\"zoneTo\":0,\"amount\":0.0,\"amountWithTransactionFee\":0.0,\"xaxis\":null,\"yaxis\":null,\"signature\":{\"v\":0,\"r\":\"\",\"s\":\"\",\"pub\":\"\"}}",jsonDataString);
        assertEquals("{\"transactiontype\":\"RegularTransaction\",\"type\":\"REWARDS\",\"status\":\"PENDING\",\"timestamp\":\"\",\"hash\":\"hash2\",\"nonce\":0,\"blockNumber\":0,\"from\":\"\",\"to\":\"\",\"zoneFrom\":0,\"zoneTo\":0,\"amount\":0.0,\"amountWithTransactionFee\":0.0,\"xaxis\":0,\"yaxis\":0,\"signature\":{\"v\":0,\"r\":\"\",\"s\":\"\",\"pub\":\"\"}}",jsonDataString2);
    }

    @Test
    public void test2() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ECDSASignatureData signatureData = new ECDSASignatureData();
        signatureData.setV((byte) 0);
        signatureData.setR(new BigInteger("30179190089666276834887403079562508974417649980904472865724382004973443579854").toByteArray());
        signatureData.setS(new BigInteger("14029798542497621816798343676332730497595770105064178818079147459382128035034").toByteArray());

        Transaction transaction=new RegularTransaction("hash1",TransactionType.REGULAR,StatusType.PENDING,0,0,"","","",0,0,0,signatureData);
        String jsonDataString = mapper.writeValueAsString(transaction);

        assertEquals("{\"transactiontype\":\"RegularTransaction\",\"type\":\"REGULAR\",\"status\":\"PENDING\",\"timestamp\":\"\",\"hash\":\"hash1\",\"nonce\":0,\"blockNumber\":0,\"from\":\"\",\"to\":\"\",\"zoneFrom\":0,\"zoneTo\":0,\"amount\":0.0,\"amountWithTransactionFee\":0.0,\"xaxis\":null,\"yaxis\":null,\"signature\":{\"v\":0,\"r\":\"\",\"s\":\"\",\"pub\":\"\"}}",jsonDataString);
    }
    @Test
    public void JsonStringToObj() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json="{\"transactiontype\":\"RegularTransaction\",\"type\":\"REGULAR\",\"status\":\"PENDING\",\"timestamp\":\"\",\"hash\":\"1\",\"nonce\":0,\"blockNumber\":0,\"from\":\"\",\"to\":\"\",\"zoneFrom\":0,\"zoneTo\":1,\"amount\":100,\"amountWithTransactionFee\":0,\"xaxis\":\"35731204215685837694879844872296111590895060163245219298330705673826162753728\",\"yaxis\":\"49582035535449768160886819563477393966724456482342997495118101870440116576649\",\"signature\":{\"v\":0,\"r\":\"47189390069456831666508435650501705407903156502375960340226480869561131339449\",\"s\":\"22705834723971466586800775033200199851248069686934755506009206490125005914447\",\"pub\":\"\"}}";
        Transaction transaction = mapper.readValue(json, Transaction.class);
        int g=3;
    }


}
