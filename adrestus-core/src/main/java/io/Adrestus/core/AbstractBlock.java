package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlock;

import java.sql.Timestamp;

public abstract class AbstractBlock implements BlockFactory, DisruptorBlock {

    private Header HeaderData;
    private StatusType Statustype;
    private String Hash;
    private int Size;
    private int Height;
    private int Generation;
    private int ViewID;


    public AbstractBlock(Header headerData, String hash, int size, int height, int generation, int viewID) {
        this.HeaderData = headerData;
        this.Hash = hash;
        this.Size = size;
        this.Height = height;
        this.Generation = generation;
        this.ViewID = viewID;
    }

    public AbstractBlock(String previousHash, int height, int generation) {
        this.HeaderData = new Header(previousHash);
        this.Height = height;
        this.Generation = generation;
    }

    public AbstractBlock(String Hash,String previousHash, int size,int height, Timestamp timestamp) {
        this.Hash=Hash;
        this.HeaderData = new Header(previousHash,timestamp);
        this.Height = height;
        this.Size = size;
    }

    public AbstractBlock() {
    }

    public String getHash() {
        return Hash;
    }

    public void setHash(String hash) {
        Hash = hash;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }


    public int getHeight() {
        return Height;
    }

    public void setHeight(int height) {
        Height = height;
    }

    public int getGeneration() {
        return Generation;
    }

    public void setGeneration(int generation) {
        Generation = generation;
    }

    public int getViewID() {
        return ViewID;
    }

    public void setViewID(int viewID) {
        ViewID = viewID;
    }

    public Header getHeaderData() {
        return HeaderData;
    }

    public void setHeaderData(Header headerData) {
        HeaderData = headerData;
    }


    public StatusType getStatustype() {
        return Statustype;
    }

    public void setStatustype(StatusType statustype) {
        Statustype = statustype;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBlock that = (AbstractBlock) o;
        return Size == that.Size && Height == that.Height && Generation == that.Generation && ViewID == that.ViewID && Objects.equal(HeaderData, that.HeaderData) && Statustype == that.Statustype && Objects.equal(Hash, that.Hash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(HeaderData, Statustype, Hash, Size, Height, Generation, ViewID);
    }


    @Override
    public String toString() {
        return "AbstractBlock{" +
                "HeaderData=" + HeaderData +
                ", Statustype=" + Statustype +
                ", Hash='" + Hash + '\'' +
                ", Size=" + Size +
                ", Height=" + Height +
                ", Generation=" + Generation +
                ", ViewID=" + ViewID +
                '}';
    }


    public final class Header{
        private int Version;
        private String PreviousHash;
        private Timestamp timestamp;

        public Header(int version, String previousHash, Timestamp timestamp) {
            this.Version = version;
            this.PreviousHash = previousHash;
            this.timestamp = timestamp;
        }

        public Header(String previousHash, Timestamp timestamp) {
            this.PreviousHash = previousHash;
            this.timestamp = timestamp;
        }

        public Header(String previousHash) {
            PreviousHash = previousHash;
        }

        public int getVersion() {
            return Version;
        }

        public void setVersion(int version) {
            Version = version;
        }

        public String getPreviousHash() {
            return PreviousHash;
        }

        public void setPreviousHash(String previousHash) {
            PreviousHash = previousHash;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Header header = (Header) o;
            return Version == header.Version && com.google.common.base.Objects.equal(PreviousHash, header.PreviousHash) && com.google.common.base.Objects.equal(timestamp, header.timestamp);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(Version, PreviousHash, timestamp);
        }

        @Override
        public String toString() {
            return "Header{" +
                    "Version=" + Version +
                    ", PreviousHash='" + PreviousHash + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
