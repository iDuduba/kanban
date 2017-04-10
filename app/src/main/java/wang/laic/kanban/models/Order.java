package wang.laic.kanban.models;

import com.google.gson.annotations.Expose;

import java.util.Date;
import java.util.List;

/**
 * Created by duduba on 2017/3/30.
 */

public class Order {
    @Expose
    private String orderNo;

    @Expose
    private int deliveryNumber;

    @Expose
    private int status;

    @Expose
    private String deliveryDate;

    @Expose
    private String arrivalDate;

    @Expose
    private List<Part> items;


    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public int getDeliveryNumber() {
        return deliveryNumber;
    }

    public void setDeliveryNumber(int deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(String arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public List<Part> getItems() {
        return items;
    }

    public void setItems(List<Part> items) {
        this.items = items;
    }
}
