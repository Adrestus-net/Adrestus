package io.Adrestus.p2p.kademlia.common;

import com.google.common.base.Objects;
import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class NettyConnectionInfo implements ConnectionInfo {
    private String host;
    private int port;

    public NettyConnectionInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public NettyConnectionInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NettyConnectionInfo that = (NettyConnectionInfo) o;
        return getPort() == that.getPort() && Objects.equal(getHost(), that.getHost());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getHost(), getPort());
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
