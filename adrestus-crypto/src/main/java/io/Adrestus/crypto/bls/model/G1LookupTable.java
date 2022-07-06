package io.Adrestus.crypto.bls.model;

public class G1LookupTable {

    private G1[] table;
    
    public G1LookupTable(G1 A) {        
        G1[] Ai = new G1[8];
        
        Ai[0] = new G1(A);
        
        G1 A2 = new G1(A);
        A2.dbl();
        
        for(int i = 0; i < 7; i++) {
            Ai[i+1] = new G1(Ai[i]);
            Ai[i+1].add(A2);
        }
        this.table = Ai;
    }
    
    public G1 select(int x) {
        return this.table[x/2];
    }
}
