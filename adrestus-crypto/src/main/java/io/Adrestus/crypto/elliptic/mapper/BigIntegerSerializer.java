package io.Adrestus.crypto.elliptic.mapper;

import io.activej.codegen.expression.Expression;
import io.activej.codegen.expression.Variable;
import io.activej.serializer.AbstractSerializerDef;
import io.activej.serializer.CompatibilityLevel;

import java.math.BigInteger;

import static io.activej.codegen.expression.Expressions.*;
import static io.activej.codegen.expression.Expressions.constructor;
import static io.activej.serializer.impl.SerializerExpressions.*;
import static io.activej.serializer.impl.SerializerExpressions.readBytes;

public class BigIntegerSerializer extends AbstractSerializerDef {

    @Override
    public Class<?> getEncodeType() {
        return BigInteger.class;
    }

    @Override
    public Expression encoder(final StaticEncoders staticEncoders,
                              final Expression buf,
                              final Variable pos,
                              final Expression big,
                              final int version,
                              final CompatibilityLevel compatibilityLevel) {
        Expression string = call(big, "toByteArray");
        return let(string, bytes -> sequence(
                writeVarInt(buf, pos, length(bytes)),
                writeBytes(buf, pos, bytes)));
    }

    @Override
    public Expression decoder(final StaticDecoders staticDecoders,
                              final Expression in,
                              final int version,
                              final CompatibilityLevel compatibilityLevel) {
        //  return staticCall(ECP.class, "fromBytes", readBytes(input,input));
        return let(arrayNew(byte[].class, readVarInt(in)), array ->
                sequence(readBytes(in, array),constructor(BigInteger.class,array)));
    }
}
