package io.Adrestus.crypto.bls.model;

public class G1LookupTable {

    private G1Point[] table;
    
    public G1LookupTable(G1Point A) {
        G1Point[] Ai = new G1Point[8];
        
        Ai[0] = new G1Point(A);
        
        G1Point A2 = new G1Point(A);
        A2.dbl();
        
        for(int i = 0; i < 7; i++) {
            Ai[i+1] = new G1Point(Ai[i]);
            Ai[i+1].add(A2);
        }
        this.table = Ai;
    }
    
    public G1Point select(int x) {
        return this.table[x/2];
    }
}
