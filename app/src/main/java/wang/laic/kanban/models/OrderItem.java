package wang.laic.kanban.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by duduba on 2017/4/10.
 */

public class OrderItem {

    @Expose
    @SerializedName("comp_id")
    private OrderKey orderCompId;

    @Expose
    private String description;

    @Expose
    private String itemCode;

    @Expose
    private String extpn;

    @Expose
    private int sendQuantity;

    @Expose
    private int arriveQuantity;

    public OrderKey getOrderCompId() {
        return orderCompId;
    }

    public void setOrderCompId(OrderKey orderCompId) {
        this.orderCompId = orderCompId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getExtpn() {
        return extpn;
    }

    public void setExtpn(String extpn) {
        this.extpn = extpn;
    }

    public int getSendQuantity() {
        return sendQuantity;
    }

    public void setSendQuantity(int sendQuantity) {
        this.sendQuantity = sendQuantity;
    }

    public int getArriveQuantity() {
        return arriveQuantity;
    }

    public void setArriveQuantity(int arriveQuantity) {
        this.arriveQuantity = arriveQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
