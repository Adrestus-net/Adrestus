package io.Adrestus.crypto.bls.model;

public class G2LookupTable {

    private G2Point[] table;

    public G2LookupTable(G2Point A) {
        G2Point[] Ai = new G2Point[8];

        Ai[0] = new G2Point(A);
        G2Point A2 = new G2Point(A);
        A2.dbl();

        for (int i = 0; i < 7; i++) {
            Ai[i + 1] = new G2Point(Ai[i]);
            Ai[i + 1].add(A2);
        }
        this.table = Ai;
    }

    public G2Point select(int x) {
        return this.table[x / 2];
    }
}
