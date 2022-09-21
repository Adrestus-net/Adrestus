package io.Adrestus.util;

import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;

public class Just4Test {
    private String var;
    private String var2;

    private ArrayList<String> list;

    public Just4Test(String var, String var2) {
        this.var = var;
        this.var2 = var2;
        list = new ArrayList<>();
    }

    public Just4Test() {
        this.var = "";
        this.var2 = "";
    }

    @Serialize
    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Serialize
    public String getVar2() {
        return var2;
    }

    public void setVar2(String var2) {
        this.var2 = var2;
    }

    @Serialize
    public ArrayList<String> getList() {
        return list;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "Test{" +
                "var='" + var + '\'' +
                ", var2='" + var2 + '\'' +
                '}';
    }
}
