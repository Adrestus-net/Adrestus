package io.Adrestus.mapper;

import io.Adrestus.MemoryTreePool;
import io.activej.serializer.*;
import io.activej.serializer.def.SimpleSerializerDef;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;

@Getter
public class MemoryTreePoolSerializer extends SimpleSerializerDef<MemoryTreePool> {
    private final ThreadSafeFury fury;

    static {
        LoggerFactory.disableLogging();
    }

    public MemoryTreePoolSerializer() {
        this.fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .withRefTracking(false)
                .withRefCopy(false)
                .withLongCompressed(true)
                .withIntCompressed(true)
                .withStringCompressed(true)
                .withScalaOptimizationEnabled(true)
                .withClassVersionCheck(false)
                .withCompatibleMode(CompatibleMode.SCHEMA_CONSISTENT)
                .withAsyncCompilation(false)
                .withCodegen(false)
                .requireClassRegistration(false)
                .buildThreadSafeFury();
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
