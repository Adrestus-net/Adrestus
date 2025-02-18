package io.Adrestus.crypto.elliptic.mapper;

import io.activej.serializer.*;
import io.activej.serializer.def.SimpleSerializerDef;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;

import java.util.TreeMap;

@Getter
public class CustomSerializerTreeMap<K, V> extends SimpleSerializerDef<TreeMap<K, V>> {
    private final ThreadSafeFury fury;

    static {
        LoggerFactory.disableLogging();
    }

    public CustomSerializerTreeMap() {
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
    protected BinarySerializer<TreeMap<K, V>> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<TreeMap<K, V>>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, TreeMap<K, V> item) {
                byte[] bytes = fury.serialize(item);
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @SneakyThrows
            @Override
            public TreeMap<K, V> decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                TreeMap<K, V> map = (TreeMap<K, V>) fury.deserialize(bytes);
                return map;
            }
        };
    }
}