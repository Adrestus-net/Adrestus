package io.Adrestus.crypto;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class KeccakSponge {

    Keccak1600 keccak1600;

    int domainPaddingBitLength;
    byte domainPadding;

    private int ratePos;

    SqueezeStream squeezeStream;
    AbsorbStream absorbStream;

    private final class SqueezeStream extends BitInputStream {
        private boolean closed = true;

        public SqueezeStream() {

        }

        @Override
        public void close() {
            if(!closed) {
                keccak1600.clear();
                closed = true;
                ratePos = 0;
            }
        }

        void open() {
            if(closed) {
                if(absorbStream != null)
                    absorbStream.close();

                ratePos = 0;
                closed = false;
            }
        }

        @Override
        public long readBits(byte[] bits, long bitOff, long bitLen) {
            open();
            long rv = 0;
            while(bitLen > 0) {
                int remainingBits = keccak1600.remainingBits(ratePos);
                if(remainingBits <=  0) {
                    keccak1600.permute();
                    ratePos = 0;
                    remainingBits = keccak1600.remainingBits(ratePos);
                }
                int chunk = (int) Math.min(bitLen, remainingBits);

                if((ratePos & 7)==0 && (bitOff&7)==0 && (chunk&7)==0) {
                    keccak1600.getBytes(ratePos>>3, bits, (int) (bitOff>>3), chunk>>3);
                } else {
                    keccak1600.getBits(ratePos, bits, bitOff, chunk);
                }

                ratePos += chunk;
                bitLen -= chunk;
                bitOff += chunk;
                rv += chunk;
            }

            return rv;
        }

        @Override
        public long transformBits(byte[] input, long inputOff, byte[] output, long outputOff, long bitLen) {
            long rv = 0;
            while(bitLen > 0) {
                int remainingBits = keccak1600.remainingBits(ratePos);
                if(remainingBits <=  0) {
                    keccak1600.permute();
                    ratePos = 0;
                    remainingBits = keccak1600.remainingBits(ratePos);
                }
                int chunk = (int) Math.min(bitLen, remainingBits);

                if((ratePos & 7)==0 && (inputOff&7)==0 && (outputOff&7)==0 && (chunk&7)==0) {
                    keccak1600.bytesOp(KeccakStateUtils.StateOp.XOR_TRANSFORM, ratePos>>3, output, (int) (outputOff>>3), input, (int) (inputOff>>3), chunk>>3);
                } else {
                    keccak1600.bitsOp(KeccakStateUtils.StateOp.XOR_TRANSFORM, ratePos, output, outputOff, input, inputOff, chunk);
                }

                ratePos += chunk;
                bitLen -= chunk;
                inputOff += chunk;
                outputOff += chunk;
                rv += chunk;
            }
            return rv;
        }
    }

    private final class AbsorbStream extends BitOutputStream {
        private boolean closed = false;

        @Override
        public void close() {
            if(!closed){
                keccak1600.pad(domainPadding, domainPaddingBitLength, ratePos);
                keccak1600.permute();
                closed = true;
                ratePos = 0;
            }
        }

        @Override
        public void writeBits(byte[] bits, long bitOff, long bitLen) {
            open();
            while(bitLen > 0) {
                int remainingBits = keccak1600.remainingBits(ratePos);
                if(remainingBits <=  0) {
                    keccak1600.permute();
                    ratePos = 0;
                    remainingBits = keccak1600.remainingBits(ratePos);
                }
                int chunk = (int) Math.min(bitLen, remainingBits);

                if((ratePos & 7)==0 && (bitOff&7)==0 && (chunk&7)==0) {
                    keccak1600.setXorBytes(ratePos>>3, bits, (int) (bitOff>>3), chunk>>3);
                } else {
                    keccak1600.setXorBits(ratePos, bits, bitOff, chunk);
                }

                ratePos += chunk;
                bitLen -= chunk;
                bitOff += chunk;
            }
        }

        public void open() {
            if(closed) {
                if(squeezeStream != null) {
                    squeezeStream.close();
                } else {
                    keccak1600.clear();
                    ratePos = 0;
                }
                closed = false;
            }

        }
    }

    public KeccakSponge(int capacityInBits, byte domainPadding, int domainPaddingBitLength) {
        this.keccak1600 = new Keccak1600(capacityInBits);
        this.domainPadding = domainPadding;
        this.domainPaddingBitLength = domainPaddingBitLength;
        this.ratePos = 0;
    }

    public KeccakSponge(int rounds, int capacityInBits, byte domainPadding, int domainPaddingBitLength) {
        this.keccak1600 = new Keccak1600(capacityInBits, rounds);
        this.domainPadding = domainPadding;
        this.domainPaddingBitLength = domainPaddingBitLength;
        this.ratePos = 0;
    }

    public void reset() {
        if(absorbStream != null) {
            absorbStream.open();
        }
    }

    public BitInputStream getSqueezeStream() {
        if(squeezeStream == null) {
            squeezeStream = new SqueezeStream();
        }
        squeezeStream.open();

        return squeezeStream;
    }

    public BitOutputStream getAbsorbStream() {
        if(absorbStream == null) {
            absorbStream = new AbsorbStream();
        }
        absorbStream.open();

        return absorbStream;
    }

    public java.io.FilterOutputStream getTransformingSqueezeStream(final java.io.OutputStream target) {
        return new FilterOutputStream(target) {
            byte[] buf = new byte[4096];

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                while(len > 0) {
                    int chunk = Math.min(len, buf.length);
                    getSqueezeStream().transform(b, off, buf, 0, chunk);
                    target.write(buf, 0, chunk);
                    off += chunk;
                    len -= chunk;
                }
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(int b) throws IOException {
                target.write(b ^ getSqueezeStream().read());
            }

            @Override
            public void close() throws IOException {
                buf = null;
                getSqueezeStream().close();
                super.close();
            }
        };

    }

    public byte[] getRateBits(int boff, int len)
    {
        byte[] rv = new byte[(len+ (8 - len & 7)) >> 3];
        keccak1600.getBits(boff, rv, boff, len);
        return rv;
    }

    public int getRateBits() {
        return keccak1600.getRateBits();
    }

    public abstract class BitInputStream extends InputStream {
        @Override
        public abstract void close();


        @Override
        public int read(byte[] b, int off, int len) {
            return (int) (readBits(b, ((long)off)<<3, ((long)len)<<3)>>3);
        }

        /**
         * Transform input to output with the input stream as a keystream
         *
         * @param input Input byte-array
         * @param inputOff Input offset
         * @param output Output byte-array
         * @param outputOff Output offset
         * @param len length in bytes
         * @return Number of bytes transformed
         */
        public int transform(byte[] input, int inputOff, byte[] output, int outputOff, int len) {
            return (int) (transformBits(input, ((long)inputOff)<<3, output, ((long)outputOff)<<3, ((long)len)<<3)>>3);
        }


        @Override
        public int read(byte[] b)  {
            return this.read(b, 0, b.length)>>3;
        }

        /**
         * Transform input to output using the input stream as a keystream
         *
         * @param input Input byte array
         * @param output Output byte array
         *
         * @return Number of bytes transformed
         */
        public int transform(byte[] input, byte[] output) {
            return (int) (transformBits(input, 0l, output, 0l, ((long)input.length)<<3)>>3);
        }


        @Override
        public int read() {
            byte[] buf = new byte[1];
            readBits(buf, 0, 8);

            return ((int) buf[0]) & 0xff;
        }


        public abstract long readBits(byte[] arg, long bitOff, long bitLen);

        /**
         * Transform input to output using the input stream as a keystream
         *
         * @param input Input byte array
         * @param inputOff Input offset in bits
         * @param output Output byte array
         * @param outputOff Output offset in bits
         * @param bitLen Number of bits
         * @return Number of bits transformed
         */
        public abstract long transformBits(byte[] input, long inputOff, byte[] output, long outputOff, long bitLen);
    }
    public abstract class BitOutputStream extends OutputStream {

        @Override
        public abstract void close();

        @Override
        public void write(byte[] b, int off, int len) {
            writeBits(b, ((long) (off))<<3, ((long)len)<<3);
        }

        @Override
        public void write(byte[] b) {
            write(b, 0, b.length);
        }

        @Override
        public void write(int b) {
            writeBits(new byte[] { (byte) b }, 0, 8);
        }

        public abstract void writeBits(byte[] arg, long bitOff, long bitLen);

    }
}
