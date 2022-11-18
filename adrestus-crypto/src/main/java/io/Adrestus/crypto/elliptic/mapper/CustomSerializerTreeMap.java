package io.Adrestus.crypto.elliptic.mapper;

import io.Adrestus.crypto.SecurityAuditProofs;
import io.activej.serializer.*;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TreeMap;

public class CustomSerializerTreeMap extends SimpleSerializerDef<TreeMap<Double, SecurityAuditProofs>> {


    @Override
    protected BinarySerializer<TreeMap<Double, SecurityAuditProofs>> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<TreeMap<Double, SecurityAuditProofs>>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, TreeMap<Double, SecurityAuditProofs> item) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream outstream = new ObjectOutputStream(byteOut);
                outstream.writeObject(item);
                byte[] bytes = byteOut.toByteArray();
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @SneakyThrows
            @Override
            public TreeMap<Double, SecurityAuditProofs> decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream instream = new ObjectInputStream(byteIn);
                TreeMap<Double, SecurityAuditProofs> treemap = (TreeMap<Double, SecurityAuditProofs>) instream.readObject();
                return treemap;
            }
        };
    }
}
