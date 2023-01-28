package io.Adrestus.crypto.elliptic.mapper;

import io.Adrestus.crypto.SecurityAuditProofs;
import io.activej.serializer.*;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TreeMap;

public class CustomSerializerTreeMap extends SimpleSerializerDef<TreeMap<StakingData, SecurityAuditProofs>> {


    @Override
    protected BinarySerializer<TreeMap<StakingData, SecurityAuditProofs>> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<TreeMap<StakingData, SecurityAuditProofs>>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, TreeMap<StakingData, SecurityAuditProofs> item) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream outstream = new ObjectOutputStream(byteOut);
                outstream.writeObject(item);
                byte[] bytes = byteOut.toByteArray();
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @SneakyThrows
            @Override
            public TreeMap<StakingData, SecurityAuditProofs> decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream instream = new ObjectInputStream(byteIn);
                TreeMap<StakingData, SecurityAuditProofs> treemap = (TreeMap<StakingData, SecurityAuditProofs>) instream.readObject();
                return treemap;
            }
        };
    }
}
