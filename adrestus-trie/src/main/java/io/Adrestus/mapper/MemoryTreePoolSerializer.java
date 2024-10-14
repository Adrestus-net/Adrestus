package io.Adrestus.mapper;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.crypto.elliptic.mapper.CustomFurySerializer;
import io.activej.serializer.*;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TreeMap;

public class MemoryTreePoolSerializer extends SimpleSerializerDef<MemoryTreePool> {
    private final Fury fury;

    static {
        LoggerFactory.disableLogging();
    }

    public MemoryTreePoolSerializer() {
        this.fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .withRefTracking(false)
                .withClassVersionCheck(true)
                .withCompatibleMode(CompatibleMode.SCHEMA_CONSISTENT)
                .withAsyncCompilation(true)
                .withCodegen(false)
                .requireClassRegistration(false)
                .build();
    }

    @Override
    protected BinarySerializer<MemoryTreePool> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<MemoryTreePool>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, MemoryTreePool item) {
                byte[] bytes = fury.serialize(item);
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @SneakyThrows
            @Override
            public MemoryTreePool decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                return (MemoryTreePool) fury.deserialize(bytes);
            }
        };
    }
}
