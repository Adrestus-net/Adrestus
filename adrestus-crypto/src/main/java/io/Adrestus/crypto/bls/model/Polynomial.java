package io.Adrestus.crypto.bls.model;

import org.apache.milagro.amcl.BLS381.BIG;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Polynomial {

    private List<FieldElement> coefficients;
    
    public Polynomial(int entropy, int degree) {
        this.coefficients = new ArrayList<>();
        
        for(int i = 0 ; i < degree+1; i++) {
            this.coefficients.add(new FieldElement(entropy));
        }
    }
    
    public FieldElement eval(FieldElement x) {
        if(x.isZero()) {
            return new FieldElement(coefficients.get(0));
        }else {
            List<FieldElement> exp = FieldElement.newVandermondeVector(x, this.coefficients.size());
            return FieldElement.innerProduct(coefficients, exp);
        }
    }
    
    public static FieldElement lagrangeBasisAt0(Set<Integer> xCoords, int i) {
        FieldElement numerator = FieldElement.one();
        FieldElement denominator = FieldElement.one();
        FieldElement negI = (new FieldElement(new BIG(i))).neg();
        for(Integer x : xCoords) {
            if(x == i) {
                continue;
            }
            FieldElement xAsFieldElem = new FieldElement(new BIG(x));
            numerator = numerator.mul(xAsFieldElem);
            FieldElement xMinusI = xAsFieldElem.plus(negI);
            denominator = denominator.mul(xMinusI);
        }
        
        denominator.inverse();
        return numerator.mul(denominator);
    }
}
