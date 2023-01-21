package io.Adrestus.consensus.ChangeView;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Serialize;

public class ChangeViewData {

    private String prev_hash;
    private int ViewID;

    public ChangeViewData() {
        this.prev_hash="";
        this.ViewID=0;
    }

    public ChangeViewData(String prev_hash, int viewID) {
        this.prev_hash = prev_hash;
        ViewID = viewID;
    }

    @Serialize
    public String getPrev_hash() {
        return prev_hash;
    }

    public void setPrev_hash(String prev_hash) {
        this.prev_hash = prev_hash;
    }

    @Serialize
    public int getViewID() {
        return ViewID;
    }

    public void setViewID(int viewID) {
        ViewID = viewID;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeViewData that = (ChangeViewData) o;
        return ViewID == that.ViewID && Objects.equal(prev_hash, that.prev_hash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(prev_hash, ViewID);
    }

    @Override
    public String toString() {
        return "ChangeViewData{" +
                "prev_hash='" + prev_hash + '\'' +
                ", ViewID=" + ViewID +
                '}';
    }
}
