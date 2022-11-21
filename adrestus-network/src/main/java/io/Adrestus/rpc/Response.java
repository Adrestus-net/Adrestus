package io.Adrestus.rpc;

import com.google.common.base.Objects;
import io.Adrestus.core.AbstractBlock;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.List;

public class Response {
    private List<AbstractBlock> abstractBlock;

    public Response(@Deserialize("abstractBlock") List<AbstractBlock> abstractBlock) {
        this.abstractBlock = abstractBlock;
    }


    public void setAbstractBlock(List<AbstractBlock> abstractBlock) {
        this.abstractBlock = abstractBlock;
    }

    @Serialize
    public List<AbstractBlock> getAbstractBlock() {
        return abstractBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return Objects.equal(abstractBlock, response.abstractBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(abstractBlock);
    }

    @Override
    public String toString() {
        return "Response{" +
                "abstractBlock=" + abstractBlock +
                '}';
    }
}
