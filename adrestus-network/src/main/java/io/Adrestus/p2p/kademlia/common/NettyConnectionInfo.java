package io.Adrestus.p2p.kademlia.common;

import com.google.common.base.Objects;
import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.activej.serializer.annotations.Serialize;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NettyConnectionInfo implements ConnectionInfo {
    private String host;
    private int port;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NettyConnectionInfo that = (NettyConnectionInfo) o;
        return getPort() == that.getPort() && Objects.equal(getHost(), that.getHost());
    }

    @Serialize
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Serialize
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getHost(), getPort());
    }
}
