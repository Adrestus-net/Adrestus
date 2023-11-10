package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.Trie.MerkleProofs;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;

public class Receipt implements Serializable {

    private int zoneFrom;
    private int zoneTo;
    private ReceiptBlock receiptBlock;
    private Transaction transaction;
    private int position;
    private MerkleProofs proofs;
    private String address;
    private Double amount;


    public Receipt() {
        this.zoneFrom = 0;
        this.zoneTo = 0;
        this.receiptBlock = new ReceiptBlock();
        this.transaction = new RegularTransaction("");
        this.position = 0;
        this.proofs = new MerkleProofs();
        this.address = "";
        this.amount = 0.0;
    }

    public Receipt(int zoneFrom,
                   int zoneTo,
                   ReceiptBlock receiptBlock,
                   Transaction transaction,
                   int position,
                   MerkleProofs proofs,
                   String address,
                   Double amount) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.receiptBlock = receiptBlock;
        this.transaction = transaction;
        this.position = position;
        this.proofs = proofs;
        this.address = address;
        this.amount = amount;
    }

    public Receipt(int zoneFrom, int zoneTo, Transaction transaction, int position, MerkleProofs proofs, String address, Double amount) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.transaction = transaction;
        this.position = position;
        this.proofs = proofs;
        this.address = address;
        this.amount = amount;
    }

    public Receipt(int zoneFrom, int zoneTo) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.proofs = new MerkleProofs();
        this.transaction = new RegularTransaction("");
        this.position = 0;
        this.address = "";
        this.amount = 0.0;
    }

    public Receipt(int zoneFrom, int zoneTo, Transaction transaction) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.transaction = transaction;
        this.position = 0;
        this.proofs = new MerkleProofs();
        this.address = "";
        this.amount = 0.0;
    }

    public Receipt(@Deserialize("zoneFrom") int zoneFrom,
                   @Deserialize("zoneTo") int zoneTo,
                   @Deserialize("address") String address,
                   @Deserialize("amount") Double amount,
                   @Deserialize("receiptBlock") ReceiptBlock receiptBlock,
                   @Deserialize("transaction") Transaction transaction,
                   @Deserialize("proofs")MerkleProofs proofs,
                   @Deserialize("position")int position) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.address = address;
        this.amount = amount;
        this.receiptBlock = receiptBlock;
        this.transaction = transaction;
        this.proofs=proofs;
        this.position = position;
    }
    public Receipt(int zoneFrom,
                   int zoneTo,
                   ReceiptBlock receiptBlock,
                   Transaction transaction) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.address = "";
        this.receiptBlock = receiptBlock;
        this.transaction = transaction;
        this.proofs=new MerkleProofs();
        this.position = 0;
        this.amount = 0.0;
    }

    public Receipt(int zoneFrom,
                  int zoneTo,
                   String address,
                   ReceiptBlock receiptBlock,
                   Transaction transaction) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.address = address;
        this.receiptBlock = receiptBlock;
        this.transaction = transaction;
        this.proofs=new MerkleProofs();
        this.position = 0;
        this.amount = 0.0;
    }

    public Receipt(Transaction transaction) {
        this.zoneFrom = 0;
        this.zoneTo = 0;
        this.transaction = transaction;
        this.proofs = new MerkleProofs();
        this.transaction = transaction;
        this.position = 0;
        this.proofs = new MerkleProofs();
        this.address = "";
        this.amount = 0.0;
    }

    @Serialize
    public int getZoneFrom() {
        return zoneFrom;
    }

    public void setZoneFrom(int zoneFrom) {
        this.zoneFrom = zoneFrom;
    }

    @Serialize
    public int getZoneTo() {
        return zoneTo;
    }

    public void setZoneTo(int zoneTo) {
        this.zoneTo = zoneTo;
    }

    @Serialize
    @SerializeNullable
    public ReceiptBlock getReceiptBlock() {
        return receiptBlock;
    }

    public void setReceiptBlock(ReceiptBlock receiptBlock) {
        this.receiptBlock = receiptBlock;
    }

    @Serialize
    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Serialize
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Serialize
    @SerializeNullable
    public MerkleProofs getProofs() {
        return proofs;
    }

    public void setProofs(MerkleProofs proofs) {
        this.proofs = proofs;
    }

    @Serialize
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Serialize
    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


    public static Receipt merge(Receipt receipt) {
        return new Receipt(
                receipt.getZoneFrom(),
                receipt.getZoneTo(),
                null,
                receipt.getTransaction(),
                receipt.getPosition(),
                receipt.getProofs(),
                receipt.getAddress(),
                receipt.getAmount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return zoneFrom == receipt.zoneFrom && zoneTo == receipt.zoneTo && position == receipt.position && Objects.equal(receiptBlock, receipt.receiptBlock) && Objects.equal(transaction, receipt.transaction) && Objects.equal(proofs, receipt.proofs) && Objects.equal(address, receipt.address) && Objects.equal(amount, receipt.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(zoneFrom, zoneTo, receiptBlock, transaction, position, proofs, address, amount);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "zoneFrom=" + zoneFrom +
                ", zoneTo=" + zoneTo +
                ", receiptBlock=" + receiptBlock +
                ", transaction=" + transaction +
                ", position=" + position +
                ", proofs=" + proofs +
                ", address='" + address + '\'' +
                ", amount=" + amount +
                '}';
    }

    public static final class ReceiptBlock implements Serializable {
        private String block_hash;
        private int height;
        private int generation;
        private String outboundMerkleRoot;


        public ReceiptBlock() {
            this.block_hash = "";
            this.height = 0;
            this.generation = 0;
            this.outboundMerkleRoot = "";
        }

        public ReceiptBlock(String block_hash, int height, int generation, String outboundMerkleRoot) {
            this.block_hash = block_hash;
            this.height = height;
            this.generation = generation;
            this.outboundMerkleRoot = outboundMerkleRoot;
        }

        @Serialize
        public String getBlock_hash() {
            return block_hash;
        }

        public void setBlock_hash(String block_hash) {
            this.block_hash = block_hash;
        }

        @Serialize
        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        @Serialize
        public int getGeneration() {
            return generation;
        }

        public void setGeneration(int generation) {
            this.generation = generation;
        }

        @Serialize
        public String getOutboundMerkleRoot() {
            return outboundMerkleRoot;
        }

        public void setOutboundMerkleRoot(String outboundMerkleRoot) {
            this.outboundMerkleRoot = outboundMerkleRoot;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReceiptBlock that = (ReceiptBlock) o;
            return height == that.height && generation == that.generation && Objects.equal(block_hash, that.block_hash) && Objects.equal(outboundMerkleRoot, that.outboundMerkleRoot);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(block_hash, height, generation, outboundMerkleRoot);
        }

        @Override
        public String toString() {
            return "ReceiptBlock{" +
                    "block_hash='" + block_hash + '\'' +
                    ", height=" + height +
                    ", generation=" + generation +
                    ", outboundMerkleRoot='" + outboundMerkleRoot + '\'' +
                    '}';
        }
    }

}


