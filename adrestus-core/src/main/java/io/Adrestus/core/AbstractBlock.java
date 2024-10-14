package io.Adrestus.core;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlock;
import io.Adrestus.core.comparators.SortSignatureMapByBlsPublicKey;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeClass;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;

@SerializeClass(subclasses = {CommitteeBlock.class, TransactionBlock.class})
public abstract class AbstractBlock extends Object implements BlockFactory, DisruptorBlock, Cloneable, Serializable {
    private Header header;
    private StatusType Statustype;
    private String Hash;
    private int Size;
    private int Height;
    private int Generation;
    private int ViewID;
    private String BlockProposer;
    private BLSPublicKey LeaderPublicKey;
    private TreeMap<BLSPublicKey, BLSSignatureData> signatureData;


    public AbstractBlock(Header headerData, String hash, int size, int height, int generation, int viewID) {
        this.header = headerData;
        this.Hash = hash;
        this.Size = size;
        this.Height = height;
        this.Generation = generation;
        this.ViewID = viewID;
        this.LeaderPublicKey = new BLSPublicKey();
        this.BlockProposer = "";
        this.signatureData = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
    }

    public AbstractBlock(String previousHash, int height, int generation, String blockProposer) {
        this.header = new Header(previousHash);
        this.Height = height;
        this.Generation = generation;
        this.LeaderPublicKey = new BLSPublicKey();
        this.BlockProposer = blockProposer;
        this.signatureData = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
    }

    public AbstractBlock(String Hash, String previousHash, int size, int height, String timestamp) {
        this.Hash = Hash;
        this.header = new Header(previousHash, timestamp);
        this.Size = size;
        this.Height = height;
        this.LeaderPublicKey = new BLSPublicKey();
        this.BlockProposer = "";
        this.signatureData = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
    }

    public AbstractBlock(String Hash, String previousHash, int size, int height, int generation, int viewID, String timestamp) {
        this.Hash = Hash;
        this.header = new Header(previousHash, timestamp);
        this.Height = height;
        this.Size = size;
        this.Generation = generation;
        this.ViewID = viewID;
        this.Statustype = StatusType.PENDING;
        this.LeaderPublicKey = new BLSPublicKey();
        this.BlockProposer = "";
        this.signatureData = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
    }

    public AbstractBlock() {
        this.header = new Header();
        this.Statustype = StatusType.PENDING;
        this.Hash = "";
        this.Size = 0;
        this.Height = 0;
        this.Generation = 0;
        this.ViewID = 0;
        this.LeaderPublicKey = new BLSPublicKey();
        this.BlockProposer = "";
        this.signatureData = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
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


    @Serialize
    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    @Serialize
    @SerializeNullable
    public TreeMap<BLSPublicKey, BLSSignatureData> getSignatureData() {
        return signatureData;
    }

    public void setSignatureData(TreeMap<BLSPublicKey, BLSSignatureData> signatureData) {
        this.signatureData = signatureData;
    }

    public void AddAllSignatureData(HashMap<BLSPublicKey, BLSSignatureData> signatureData) {
        this.signatureData.putAll(signatureData);
    }

    public String getBlockProposer() {
        return BlockProposer;
    }

    public void setBlockProposer(String blockProposer) {
        BlockProposer = blockProposer;
    }

    public BLSPublicKey getLeaderPublicKey() {
        return LeaderPublicKey;
    }

    public void setLeaderPublicKey(BLSPublicKey leaderPublicKey) {
        LeaderPublicKey = leaderPublicKey;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBlock block = (AbstractBlock) o;
        return Size == block.Size && Height == block.Height && Generation == block.Generation && ViewID == block.ViewID && Objects.equals(header, block.header) && Statustype == block.Statustype && Objects.equals(Hash, block.Hash) && Objects.equals(BlockProposer, block.BlockProposer) && Objects.equals(LeaderPublicKey, block.LeaderPublicKey) && Objects.equals(signatureData, block.signatureData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, Statustype, Hash, Size, Height, Generation, ViewID, BlockProposer, LeaderPublicKey, signatureData);
    }

    public static class Header implements Serializable {
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
