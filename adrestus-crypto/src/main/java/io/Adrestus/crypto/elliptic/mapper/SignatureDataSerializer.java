package io.Adrestus.crypto.elliptic.mapper;

import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.activej.bytebuf.ByteBuf;
import io.activej.serializer.BinaryInput;
import io.activej.serializer.BinaryOutput;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.CompatibilityLevel;
import io.activej.serializer.def.SimpleSerializerDef;

import java.nio.ByteBuffer;

public class SignatureDataSerializer extends SimpleSerializerDef<ECDSASignatureData> {
    @Override
    protected BinarySerializer<ECDSASignatureData> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<ECDSASignatureData>() {
            @Override
            public void encode(BinaryOutput out, ECDSASignatureData item) {
                ByteBuf byteBuf = ByteBuf.wrap(new byte[1 + item.getR().length + item.getS().length + item.getPub().length], 0, 0);
                ByteBuffer buffer = byteBuf.toWriteByteBuffer();
                buffer.put(item.getV());
                buffer.put(item.getR());
                buffer.put(item.getS());
                buffer.put(item.getPub());

                byteBuf.ofWriteByteBuffer(buffer);
                out.writeInt(byteBuf.asArray().length);
                out.writeInt(item.getR().length);
                out.writeInt(item.getS().length);
                out.writeInt(item.getPub().length);
                out.write(byteBuf.asArray());
            }

            @Override
            public ECDSASignatureData decode(BinaryInput in) {
                int finals = in.readInt();
                int r_length = in.readInt();
                int s_length = in.readInt();
                int pub_length = in.readInt();
                byte[] bytes = new byte[finals];
                in.read(bytes);

                ByteBuf byteBuf = ByteBuf.wrap(bytes, 0, bytes.length);
                ByteBuf slice_V = byteBuf.slice(0, 1);
                ByteBuf slice_r = byteBuf.slice(1, r_length);
                ByteBuf slice_s = byteBuf.slice(r_length + 1, s_length);
                ByteBuf slice_pub = byteBuf.slice(s_length + 1, pub_length);
                return new ECDSASignatureData(slice_V.getArray()[0], slice_r.getArray(), slice_s.getArray(), slice_pub.getArray());
            }
        };
    }
}
