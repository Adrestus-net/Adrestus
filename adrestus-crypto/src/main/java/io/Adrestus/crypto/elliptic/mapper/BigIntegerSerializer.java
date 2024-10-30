package io.Adrestus.crypto.elliptic.mapper;


import io.activej.codegen.expression.Expression;
import io.activej.codegen.expression.Variable;
import io.activej.serializer.CompatibilityLevel;
import io.activej.serializer.def.AbstractSerializerDef;

import java.math.BigInteger;

import static io.activej.codegen.expression.Expressions.*;
import static io.activej.serializer.def.SerializerExpressions.*;

public class BigIntegerSerializer extends AbstractSerializerDef {

    @Override
    public Class<?> getEncodeType() {
        return BigInteger.class;
    }

    @Override
    public Expression encode(StaticEncoders staticEncoders, Expression buf, Variable pos, Expression big, int i, CompatibilityLevel compatibilityLevel) {
        Expression string = call(big, "toByteArray");
        return let(string, bytes -> sequence(
                writeVarInt(buf, pos, length(bytes)),
                writeBytes(buf, pos, bytes)));
    }

    @Override
    public Expression decode(StaticDecoders staticDecoders, Expression in, int i, CompatibilityLevel compatibilityLevel) {
        //  return staticCall(ECP.class, "fromBytes", readBytes(input,input));
        return let(arrayNew(byte[].class, readVarInt(in)), array ->
                sequence(readBytes(in, array), constructor(BigInteger.class, array)));
    }
}
