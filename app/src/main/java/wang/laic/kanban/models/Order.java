package wang.laic.kanban.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by duduba on 2017/3/30.
 */

public class Order {

    @Expose
    @SerializedName("comp_id")
    private OrderKey orderCompId;

    @Expose
    private int status;

    @Expose
    private String location;

    @Expose
    @SerializedName("sendDate")
    private Date deliveryDate;

    @Expose
    @SerializedName("arriveDate")
    private Date arrivalDate;

    @Expose
    @SerializedName("sendItemList")
    private List<OrderItem> items;

    public OrderKey getOrderCompId() {
        return orderCompId;
    }

    public void setOrderCompId(OrderKey orderCompId) {
        this.orderCompId = orderCompId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderCompId=" + orderCompId +
                '}';
    }
}
