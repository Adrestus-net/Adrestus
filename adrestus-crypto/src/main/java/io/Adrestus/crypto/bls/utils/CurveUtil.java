package io.Adrestus.crypto.bls.utils;
import io.Adrestus.crypto.bls.model.FieldElement;
import io.Adrestus.crypto.bls.model.G1;
import io.Adrestus.crypto.bls.model.G2;
import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP;
import org.apache.milagro.amcl.BLS381.ECP2;
import org.apache.milagro.amcl.BLS381.ROM;

public class CurveUtil {
    public static final BIG P = new BIG(ROM.Modulus);
    public static final FieldElement curveOrder = new FieldElement(new BIG(ROM.CURVE_Order));
    public static final G1 g1Generator = new G1(ECP.generator());
    public static final G2 g2Generator = new G2(ECP2.generator());
}
