package io.Adrestus.crypto.bls.mapper;

import io.Adrestus.crypto.bls.BLS381.BIG;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.activej.codegen.expression.Expression;
import io.activej.codegen.expression.Variable;
import io.activej.serializer.*;

import static io.activej.codegen.expression.Expressions.*;
import static io.activej.serializer.impl.SerializerExpressions.*;

public class ECPmapper extends SimpleSerializerDef<ECP>{
    @Override
    protected BinarySerializer<ECP> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<ECP>() {
            @Override
            public void encode(BinaryOutput out, ECP item) {
                byte[] bytes = new byte[BIG.MODBYTES + 1];
                item.toBytes(bytes, true);
                out.write(bytes);
            }

            @Override
            public ECP decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[BIG.MODBYTES + 1];
                in.read(bytes);
                return ECP.fromBytes(bytes);
            }
        };
    }
}

/*public class ECPmapper extends AbstractSerializerDef {
    private static final int ARRAY_LEN = 2 * (BIG.MODBYTES + 1);
    private static final Expression ARRAY_EXPRESSION = value(new byte[ARRAY_LEN]);
    private static final Expression BOOLEAN_EXPRESSION = value(false);

    @Override
    public Class<?> getEncodeType() {
        return ECP.class;
    }

    @Override
    public Expression encoder(final SerializerDef.StaticEncoders staticEncoders,
                              final Expression buf,
                              final Variable pos,
                              final Expression ecp,
                              final int version,
                              final CompatibilityLevel compatibilityLevel) {
        Expression expression = call(ecp, "getBytes", ARRAY_EXPRESSION, BOOLEAN_EXPRESSION);
        return let(expression, bytes -> sequence(
                writeVarInt(buf, pos, length(bytes)),
                writeBytes(buf, pos, bytes)));
    }

    @Override
    public Expression decoder(final SerializerDef.StaticDecoders staticDecoders,
                              final Expression in,
                              final int version,
                              final CompatibilityLevel compatibilityLevel) {
        return let(arrayNew(byte[].class, readVarInt(in)), array ->
                sequence(readBytes(in, array), staticCall(ECP.class, "fromBytes", array)));
    }
}*/
