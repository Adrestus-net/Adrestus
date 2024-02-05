package io.Adrestus.erasure.code;

import io.Adrestus.erasure.code.parameters.FECParameterObject;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class SerializableErasureObject {

    private FECParameterObject fecParameterObject;
    private byte[] originalPacketChunks;
    private ArrayList<byte[]> repairPacketChunks;


    public SerializableErasureObject() {
    }

    public SerializableErasureObject(FECParameterObject fecParameterObject, byte[] originalPacketChunks) {
        this.fecParameterObject = fecParameterObject;
        this.originalPacketChunks = originalPacketChunks;
        this.repairPacketChunks = new ArrayList<>();
    }

    public SerializableErasureObject(FECParameterObject fecParameterObject, byte[] originalPacketChunks, ArrayList<byte[]> repairPacketChunks) {
        this.fecParameterObject = fecParameterObject;
        this.originalPacketChunks = originalPacketChunks;
        this.repairPacketChunks = repairPacketChunks;
    }

    @Serialize
    public FECParameterObject getFecParameterObject() {
        return fecParameterObject;
    }

    public void setFecParameterObject(FECParameterObject fecParameterObject) {
        this.fecParameterObject = fecParameterObject;
    }

    @Serialize
    public byte[] getOriginalPacketChunks() {
        return originalPacketChunks;
    }

    public void setOriginalPacketChunks(byte[] originalPacketChunks) {
        this.originalPacketChunks = originalPacketChunks;
    }

    @Serialize
    public ArrayList<byte[]> getRepairPacketChunks() {
        return repairPacketChunks;
    }

    public void setRepairPacketChunks(ArrayList<byte[]> repairPacketChunks) {
        this.repairPacketChunks = repairPacketChunks;
    }

    //DO NOT DELETE THAT FUNCTION ITS CUSTOM CHECK repairPacketChunks ARRAYLIST
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializableErasureObject that = (SerializableErasureObject) o;
        return Objects.equals(fecParameterObject, that.fecParameterObject) && Arrays.equals(originalPacketChunks, that.originalPacketChunks) && SerializableErasureObject.linkedEquals(repairPacketChunks, that.repairPacketChunks);
    }

    private static boolean linkedEquals(ArrayList<byte[]>first,ArrayList<byte[]>second){
        for(int i=0;i<first.size();i++){
            if(!Arrays.equals(first.get(i),second.get(i))){
                return false;
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        int result = Objects.hash(fecParameterObject, repairPacketChunks);
        result = 31 * result + Arrays.hashCode(originalPacketChunks);
        return result;
    }
}
