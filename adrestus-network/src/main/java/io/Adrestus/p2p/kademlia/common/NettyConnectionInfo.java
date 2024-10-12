package io.Adrestus.p2p.kademlia.common;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.activej.serializer.annotations.Serialize;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public class NettyConnectionInfo implements ConnectionInfo {
    private String host;
    private int port;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NettyConnectionInfo that = (NettyConnectionInfo) o;
        return port == that.port && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "NettyConnectionInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
