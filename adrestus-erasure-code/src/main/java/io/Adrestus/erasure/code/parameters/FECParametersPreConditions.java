package io.Adrestus.erasure.code.parameters;

import io.Adrestus.erasure.code.Exceptions.CheckSymbolSizeOutOfBoundsException;

import static io.Adrestus.erasure.code.parameters.ParameterChecker.maxAllowedDataLength;
import static io.Adrestus.erasure.code.parameters.ParameterChecker.maxSymbolSize;

public class FECParametersPreConditions {


    public static FECParameterObject CalculateFECParameters(long dataLen, int symbSize, int numSrcBlks) {
        FECParameterObject object = new FECParameterObject();

        int MinAllowedSymbolSize = maxSymbolSize();
        if (symbSize > MinAllowedSymbolSize) {
            object.setSymbolSize(MinAllowedSymbolSize);

        } else {
            object.setSymbolSize(symbSize);
        }

        try {
            long maxed_data_len = maxAllowedDataLength(object.getSymbolSize());
            if (dataLen > maxed_data_len) {
                object.setDataLen(maxed_data_len);
            } else {
                object.setDataLen(dataLen);
            }
        } catch (CheckSymbolSizeOutOfBoundsException e) {
            e.printStackTrace();
        }
        object.setNumberOfSymbols(numSrcBlks);
        object.setSymbolOverhead(2);
//        try {
//            int AllowedNumSourceBlocks = maxAllowedNumSourceBlocks(object.getDataLen(), object.getSymbolSize());
//            if (numSrcBlks > AllowedNumSourceBlocks) {
//                object.setNumberOfSymbols(AllowedNumSourceBlocks);
//            } else {
//                object.setNumberOfSymbols(numSrcBlks);
//            }
//            object.setSymbolOverhead(2);
//
//        } catch (CheckDataLengthOutOfBoundsException e) {
//            e.printStackTrace();
//        } catch (CheckDataLengthAndSymbolSizeOutOfBoundsException e) {
//            throw new RuntimeException(e);
//        } catch (CheckSymbolSizeOutOfBoundsException e) {
//            throw new RuntimeException(e);
//        }

        return object;
    }
}
