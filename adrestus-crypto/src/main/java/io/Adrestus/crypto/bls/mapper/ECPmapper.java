package io.Adrestus.crypto.bls.mapper;

import io.Adrestus.crypto.bls.BLS381.BIG;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.activej.codegen.expression.Expression;
import io.activej.codegen.expression.Variable;
import io.activej.serializer.AbstractSerializerDef;
import io.activej.serializer.CompatibilityLevel;
import io.activej.serializer.SerializerDef;

import static io.activej.codegen.expression.Expressions.*;
import static io.activej.serializer.impl.SerializerExpressions.*;
import static io.activej.serializer.impl.SerializerExpressions.readBytes;

public class ECPmapper extends AbstractSerializerDef {
    private static final int ARRAY_LEN = 2*(BIG.MODBYTES + 1);
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
        Expression expression = call(ecp, "getBytes",ARRAY_EXPRESSION,BOOLEAN_EXPRESSION);
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
                sequence(readBytes(in, array),staticCall(ECP.class, "fromBytes", array)));
    }
}
