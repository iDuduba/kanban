package wang.laic.kanban.models;

import com.google.gson.annotations.Expose;

import java.util.Date;

/**
 * Created by duduba on 2017/4/5.
 */

public class Flow {
    @Expose
    private Date opDate;

    @Expose
    private int opType;

    @Expose
    private int quantity;

    public Date getOpDate() {
        return opDate;
    }

    public void setOpDate(Date opDate) {
        this.opDate = opDate;
    }

    public int getOpType() {
        return opType;
    }

    public void setOpType(int opType) {
        this.opType = opType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
