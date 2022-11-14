package io.Adrestus.core.Resourses;

import com.google.common.base.Objects;

import java.util.Arrays;

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

    public final class SecurityHeader {
        private byte[] pRnd;
        private byte[] Rnd;

        public SecurityHeader() {
        }

        public SecurityHeader(byte[] pRnd, byte[] rnd) {
            this.pRnd = pRnd;
            Rnd = rnd;
        }

        public byte[] getpRnd() {
            return pRnd;
        }

        public void setpRnd(byte[] pRnd) {
            this.pRnd = pRnd;
        }

        public byte[] getRnd() {
            return Rnd;
        }

        public void setRnd(byte[] rnd) {
            Rnd = rnd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SecurityHeader that = (SecurityHeader) o;
            return Objects.equal(pRnd, that.pRnd) && Objects.equal(Rnd, that.Rnd);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(pRnd, Rnd);
        }

        @Override
        public String toString() {
            return "SecurityHeader{" +
                    "pRnd=" + Arrays.toString(pRnd) +
                    ", Rnd=" + Arrays.toString(Rnd) +
                    '}';
        }
    }
}
