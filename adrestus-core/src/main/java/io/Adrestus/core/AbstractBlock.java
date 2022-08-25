package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlock;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeClass;

@SerializeClass(subclasses = {CommitteeBlock.class, TransactionBlock.class})
public abstract class AbstractBlock implements BlockFactory, DisruptorBlock {

    private Header header;
    private StatusType Statustype;
    private String Hash;
    private int Size;
    private int Height;
    private int Generation;
    private int ViewID;


    public AbstractBlock(Header headerData, String hash, int size, int height, int generation, int viewID) {
        this.header = headerData;
        this.Hash = hash;
        this.Size = size;
        this.Height = height;
        this.Generation = generation;
        this.ViewID = viewID;
    }

    public AbstractBlock(String previousHash, int height, int generation) {
        this.header = new Header(previousHash);
        this.Height = height;
        this.Generation = generation;
    }

    public AbstractBlock(String Hash, String previousHash, int size, int height, String timestamp) {
        this.Hash = Hash;
        this.header = new Header(previousHash, timestamp);
        this.Height = height;
        this.Size = size;
    }

    public AbstractBlock() {
        this.header = new Header();
        this.Statustype = StatusType.PENDING;
        this.Hash = "";
        this.Size = 0;
        this.Height = 0;
        this.Generation = 0;
        this.ViewID = 0;
    }

    @Serialize
    public String getHash() {
        return Hash;
    }

    public void setHash(String hash) {
        Hash = hash;
    }

    @Serialize
    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }

    @Serialize
    public int getHeight() {
        return Height;
    }

    public void setHeight(int height) {
        Height = height;
    }

    @Serialize
    public int getGeneration() {
        return Generation;
    }

    public void setGeneration(int generation) {
        Generation = generation;
    }

    @Serialize
    public int getViewID() {
        return ViewID;
    }

    public void setViewID(int viewID) {
        ViewID = viewID;
    }

    @Serialize
    public Header getHeaderData() {
        return this.header;
    }

    public void setHeaderData(Header headerData) {
        this.header = headerData;
    }

    @Serialize
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
        return Size == that.Size && Height == that.Height && Generation == that.Generation && ViewID == that.ViewID && Objects.equal(header, that.header) && Statustype == that.Statustype && Objects.equal(Hash, that.Hash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(header, Statustype, Hash, Size, Height, Generation, ViewID);
    }

    @Override
    public String toString() {
        return "AbstractBlock{" +
                "header=" + header +
                ", Statustype=" + Statustype +
                ", Hash='" + Hash + '\'' +
                ", Size=" + Size +
                ", Height=" + Height +
                ", Generation=" + Generation +
                ", ViewID=" + ViewID +
                '}';
    }

    public static class Header {
        private int Version;
        private String PreviousHash;
        private String timestamp;

        public Header(int version, String previousHash, String timestamp) {
            this.Version = version;
            this.PreviousHash = previousHash;
            this.timestamp = timestamp;
        }

        public Header(String previousHash, String timestamp) {
            this.PreviousHash = previousHash;
            this.timestamp = timestamp;
        }

        public Header() {
            this.Version = AdrestusConfiguration.version;
            this.PreviousHash = "";
            this.timestamp = "";
        }

        public Header(String previousHash) {
            PreviousHash = previousHash;
        }

        @Serialize
        public int getVersion() {
            return Version;
        }

        public void setVersion(int version) {
            Version = version;
        }

        @Serialize
        public String getPreviousHash() {
            return PreviousHash;
        }

        public void setPreviousHash(String previousHash) {
            PreviousHash = previousHash;
        }

        @Serialize
        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
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
