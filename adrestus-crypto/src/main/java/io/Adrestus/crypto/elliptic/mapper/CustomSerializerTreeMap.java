package io.Adrestus.crypto.elliptic.mapper;

import io.Adrestus.crypto.SecurityAuditProofs;
import io.activej.codegen.expression.Expression;
import io.activej.codegen.expression.Expressions;
import io.activej.serializer.*;
import lombok.SneakyThrows;

import java.io.*;
import java.util.TreeMap;

public class CustomSerializerTreeMap extends SimpleSerializerDef<TreeMap<Double, SecurityAuditProofs>> {

    private int sizes;
    @Override
    protected BinarySerializer<TreeMap<Double, SecurityAuditProofs>> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<TreeMap<Double, SecurityAuditProofs>>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, TreeMap<Double, SecurityAuditProofs> item) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream outstream = new ObjectOutputStream(byteOut);
                outstream.writeObject(item);
                out.write(byteOut.toByteArray());

                sizes=byteOut.toByteArray().length;
            }

            @SneakyThrows
            @Override
            public TreeMap<Double, SecurityAuditProofs> decode(BinaryInput in) throws CorruptedDataException {

               // byte[] bytes = new byte[in.array.length-(2*in.pos)];
                int as=in.array.length;
                byte[] bytes = new byte[sizes];
                in.read(bytes);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream instream = new ObjectInputStream(byteIn);
                TreeMap<Double, SecurityAuditProofs>treemap=(TreeMap<Double, SecurityAuditProofs>) instream.readObject();
                return treemap;
               /* int size=100;
                int count=1;
                boolean flag=false;
                int currentpos=in.array.length;
                TreeMap<Double,SecurityAuditProofs> treemap=new TreeMap<>();
                byte[] bytes = new byte[size];
                in.read(bytes);
                while(!flag){
                    try{
                        if(count!=1){
                            bytes = new byte[size+count];
                            in=new BinaryInput(in.array,currentpos);
                            in.read(bytes);
                        }
                        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                        ObjectInputStream instream = new ObjectInputStream(byteIn);
                        treemap=(TreeMap<Double, SecurityAuditProofs>) instream.readObject();
                        flag=true;
                    } catch(EOFException e) { // or your specific exception
                        count++;
                    }

                }
                return treemap;*/


                /*  byte[] in1=in.array.clone(),in2=in.array.clone(),in3=in.array.clone();

                int size=1024;
                int count=1;
                boolean flag=false;
                TreeMap<Double, String> treemap=new TreeMap<>();
                byte[] bytes = new byte[size*count];
                in.read(bytes);
                while(!flag){
                    try{
                        byte[] buffer=bytes.clone();
                        if(count!=1){
                            buffer=new byte[count*size];
                            System.arraycopy(bytes,0,buffer,0,bytes.length);
                            System.arraycopy(in1,bytes.length,buffer,bytes.length,in1.length);
                        }
                        ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer);
                        ObjectInputStream instream = new ObjectInputStream(byteIn);
                        treemap=(TreeMap<Double, String>) instream.readObject();
                        flag=true;
                    } catch(EOFException e) { // or your specific exception
                        count++;
                    }
                    catch (StreamCorruptedException ex){
                        count++;
                    }

                }
                return treemap;*/
            }
        };
    }
}
