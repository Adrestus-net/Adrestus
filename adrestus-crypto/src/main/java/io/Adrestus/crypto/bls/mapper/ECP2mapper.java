package io.Adrestus.crypto.bls.mapper;

import io.Adrestus.crypto.bls.BLS381.BIG;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.activej.serializer.*;

public class ECP2mapper extends SimpleSerializerDef<ECP2> {
    private static final int ARRAY_LEN = 4 * (BIG.MODBYTES + 1);

    protected BinarySerializer<ECP2> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<ECP2>() {
            @Override
            public void encode(BinaryOutput out, ECP2 item) {
                byte[] bytes = new byte[ARRAY_LEN];
                item.toBytes(bytes);
                out.write(bytes);
            }

            @Override
            public ECP2 decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[ARRAY_LEN];
                in.read(bytes);
                return ECP2.fromBytes(bytes);
            }
        };
    }
}
/*public class ECP2mapper extends AbstractSerializerDef {
    private static final int ARRAY_LEN = 4 * (BIG.MODBYTES + 1);
    private static final Expression ARRAY_EXPRESSION = value(new byte[ARRAY_LEN]);

    @Override
    public Class<?> getEncodeType() {
        return ECP2.class;
    }

    @Override
    public Expression encoder(final StaticEncoders staticEncoders,
                              final Expression buf,
                              final Variable pos,
                              final Expression ecp,
                              final int version,
                              final CompatibilityLevel compatibilityLevel) {
        Expression expression = call(ecp, "getBytes", ARRAY_EXPRESSION);
        return let(expression, bytes -> sequence(
                writeVarInt(buf, pos, length(bytes)),
                writeBytes(buf, pos, bytes)));
    }

    @Override
    public Expression decoder(final StaticDecoders staticDecoders,
                              final Expression in,
                              final int version,
                              final CompatibilityLevel compatibilityLevel) {
        return let(arrayNew(byte[].class, readVarInt(in)), array ->
                sequence(readBytes(in, array), staticCall(ECP2.class, "fromBytes", array)));
    }
}*/
