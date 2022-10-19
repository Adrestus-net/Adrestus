package io.Adrestus.p2p.kademlia.common;

import com.google.common.base.Objects;
import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Override
    public int hashCode() {
        return Objects.hashCode(getHost(), getPort());
    }
}
