package io.Adrestus.core.Resourses;

import com.google.common.base.Objects;
import io.Adrestus.crypto.SecurityHeader;

public class CachedSecurityHeaders {

    private static volatile CachedSecurityHeaders instance;
    private SecurityHeader securityHeader;


    private CachedSecurityHeaders() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        } else {
            this.securityHeader = new SecurityHeader();
        }
    }

    public SecurityHeader getSecurityHeader() {
        return securityHeader;
    }

    public void setSecurityHeader(SecurityHeader securityHeader) {
        this.securityHeader = securityHeader;
    }

    public static CachedSecurityHeaders getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedSecurityHeaders.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedSecurityHeaders();
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedSecurityHeaders that = (CachedSecurityHeaders) o;
        return Objects.equal(securityHeader, that.securityHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(securityHeader);
    }

    @Override
    public String toString() {
        return "CachedSecurityHeaders{" +
                "securityHeader=" + securityHeader +
                '}';
    }

}
