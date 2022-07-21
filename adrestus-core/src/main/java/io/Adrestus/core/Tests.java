package io.Adrestus.core;

import io.activej.serializer.annotations.Serialize;

public class Tests {
    private String var;
    private String var1;

    public Tests() {
    }

    @Serialize
    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Serialize
    public String getVar1() {
        return var1;
    }

    public void setVar1(String var1) {
        this.var1 = var1;
    }

    @Override
    public String toString() {
        return "Tests{" +
                "var='" + var + '\'' +
                ", var1='" + var1 + '\'' +
                '}';
    }
}
