package io.Adrestus.crypto.elliptic.mapper;

import io.activej.serializer.*;
import io.activej.serializer.def.SimpleSerializerDef;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;

import java.util.TreeMap;

public class CustomSerializerTreeMap extends SimpleSerializerDef<TreeMap<Object, Object>> {
    @Getter
    private static Fury fury;

    static {
        LoggerFactory.disableLogging();
    }

    public CustomSerializerTreeMap() {
        fury = Fury.builder()
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
    protected BinarySerializer<TreeMap<Object, Object>> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<TreeMap<Object, Object>>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, TreeMap<Object, Object> item) {
                byte[] bytes = fury.serialize(item);
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @SneakyThrows
            @Override
            public TreeMap<Object, Object> decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                TreeMap<Object, Object> map = (TreeMap<Object, Object>) fury.deserialize(bytes);
//                fury.reset();
//                fury.resetBuffer();
//                fury.resetRead();
//                fury.resetWrite();
                return map;
            }
        };
    }
}
