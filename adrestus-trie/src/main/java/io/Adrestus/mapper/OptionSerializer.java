package io.Adrestus.mapper;

import io.Adrestus.Trie.PatriciaTreeNode;
import io.activej.serializer.*;
import io.vavr.control.Option;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class OptionSerializer extends SimpleSerializerDef<Option> {

    //private SerializationUtil<PatriciaTreeNode> serializationUtil;

    public OptionSerializer() {
        //  Type fluentType = new TypeToken<PatriciaTreeNode>() {
        //  }.getType();
        //  this.serializationUtil = new SerializationUtil<PatriciaTreeNode>(PatriciaTreeNode.class);
    }

    @Override
    protected BinarySerializer<Option> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<Option>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, Option item) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream outstream = new ObjectOutputStream(byteOut);
                if (item.isDefined())
                    outstream.writeObject(item.get());
                else
                    outstream.writeObject(new PatriciaTreeNode());
                byte[] bytes = byteOut.toByteArray();
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @SneakyThrows
            @Override
            public Option decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream instream = new ObjectInputStream(byteIn);
                PatriciaTreeNode treemap = (PatriciaTreeNode) instream.readObject();
                return Option.of(treemap);
            }
        };
    }
}
