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
        Transaction transaction = new RegularTransaction("hash1", TransactionType.REGULAR, StatusType.PENDING, 0, 0, "", "", "", 0, 0, 0, new ECDSASignatureData());
        Transaction transaction2 = new RegularTransaction();
        transaction2.setHash("hash2");
        transaction2.setType(TransactionType.REWARDS);
        String jsonDataString = mapper.writeValueAsString(transaction);
        String jsonDataString2 = mapper.writeValueAsString(transaction2);

        assertEquals("{\"transactiontype\":\"RegularTransaction\",\"type\":\"REGULAR\",\"status\":\"PENDING\",\"timestamp\":\"\",\"hash\":\"hash1\",\"nonce\":0,\"blockNumber\":0,\"from\":\"\",\"to\":\"\",\"zoneFrom\":0,\"zoneTo\":0,\"amount\":0.0,\"amountWithTransactionFee\":0.0,\"xaxis\":null,\"yaxis\":null,\"signature\":{\"v\":0,\"r\":\"\",\"s\":\"\",\"pub\":\"\"},\"transactionCallback\":null}", jsonDataString);
        assertEquals("{\"transactiontype\":\"RegularTransaction\",\"type\":\"REWARDS\",\"status\":\"PENDING\",\"timestamp\":\"\",\"hash\":\"hash2\",\"nonce\":0,\"blockNumber\":0,\"from\":\"\",\"to\":\"\",\"zoneFrom\":0,\"zoneTo\":0,\"amount\":0.0,\"amountWithTransactionFee\":0.0,\"xaxis\":0,\"yaxis\":0,\"signature\":{\"v\":0,\"r\":\"\",\"s\":\"\",\"pub\":\"\"},\"transactionCallback\":null}", jsonDataString2);
    }

    @Test
    public void test2() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ECDSASignatureData signatureData = new ECDSASignatureData();
        signatureData.setV((byte) 0);
        signatureData.setR(new BigInteger("30179190089666276834887403079562508974417649980904472865724382004973443579854").toByteArray());
        signatureData.setS(new BigInteger("14029798542497621816798343676332730497595770105064178818079147459382128035034").toByteArray());

        Transaction transaction = new RegularTransaction("hash1", TransactionType.REGULAR, StatusType.PENDING, 0, 0, "", "", "", 0, 0, 0, signatureData);
        String jsonDataString = mapper.writeValueAsString(transaction);

        assertEquals("{\"transactiontype\":\"RegularTransaction\",\"type\":\"REGULAR\",\"status\":\"PENDING\",\"timestamp\":\"\",\"hash\":\"hash1\",\"nonce\":0,\"blockNumber\":0,\"from\":\"\",\"to\":\"\",\"zoneFrom\":0,\"zoneTo\":0,\"amount\":0.0,\"amountWithTransactionFee\":0.0,\"xaxis\":null,\"yaxis\":null,\"signature\":{\"v\":0,\"r\":\"QrjQ9wiWv/wkFXTZN20gdKU/cmjH1Dv1o8snhZH5f84=\",\"s\":\"HwSVpnel8+Wg/o8u9Q99fQmSvmeLNDFEG/zDhQ+mRNo=\",\"pub\":\"\"},\"transactionCallback\":null}", jsonDataString);
    }

    @Test
    public void JsonStringToObj() throws JsonProcessingException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = "{\"transactiontype\":\"RegularTransaction\",\"type\":\"REGULAR\",\"status\":\"PENDING\",\"timestamp\":\"2023-10-06 18:27:34.018\",\"hash\":\"89960884d2cf0493ff0d36734b4e34360cbcd11097db1e42950d5d86f8e92dcd\",\"nonce\":1,\"blockNumber\":0,\"from\":\"ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L\",\"to\":\"ADR-GB5Y-BF5F-JUS3-5HHG-WWQR-MCM3-LIP6-EMWY-UVAK-PXYV\",\"zoneFrom\":\"0\",\"zoneTo\":\"0\",\"amount\":10.35,\"amountWithTransactionFee\":1.035,\"xaxis\":\"28271553942235212214291489831570675874253463439955561634951606308928980622038\",\"yaxis\":\"104551439082988170774010735643455187826071264757440552210034585859594047412998\",\"signature\":{\"v\":0,\"r\":\"yh1UBatW3VBm9jlAfFDZSedlZ/I9RlfC5ekGTKAngX0=\",\"s\":\"8qe7gts3npeR9nBLZcPjVxbcE8dDEkVA7J9ajf65g/k=\",\"pub\":\"\"}}";
            Transaction transaction = mapper.reader().forType(RegularTransaction.class).readValue(json);
            int g = 3;
        } catch (NoSuchMethodError e) {
            System.out.println(e.toString());
        }
    }


}
