package wang.laic.kanban.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by duduba on 2017/3/31.
 */

public class Part {

    /*
{"code":0,"message":null,"timestamp":1491828567831,"body":
[{"customerCode":"CBJ10030",
"itemType":"0",
"itemCode":"N-XVRNW10HL1/4ED",
"description":null,
"customerSku":"231111","category":"硬管接头体","safeStock":0.0}]}
     */
    @Expose
    @SerializedName("itemCode")
    private String model;

    @Expose(deserialize = true, serialize = false)
    @SerializedName("customerSku")
    private String partNo;

    @Expose(deserialize = true, serialize = false)
    private int itemType = 0;

    @Expose(deserialize = true, serialize = false)
    @SerializedName("itemTypeName")
    private String category;

    private int opType = OpEnum.OUT.getType();

    @Expose(deserialize = false, serialize = true)
    private double quantity = 0;

    @Expose(deserialize = true, serialize = false)
    private double invQuantity = 0;

    @Expose(deserialize = true, serialize = false)
    private double safeStock = 0;

    @Expose(deserialize = true, serialize = false)
    private String location;

    @Expose(deserialize = true, serialize = false)
    private String shelfNo; // 货架号

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getOpType() {
        return opType;
    }

    public void setOpType(int opType) {
        this.opType = opType;
    }

    public double getInvQuantity() {
        return invQuantity;
    }

    public void setInvQuantity(double invQuantity) {
        this.invQuantity = invQuantity;
    }

    public double getSafeStock() {
        return safeStock;
    }

    public void setSafeStock(double safeStock) {
        this.safeStock = safeStock;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getShelfNo() {
        return shelfNo;
    }

    public void setShelfNo(String shelfNo) {
        this.shelfNo = shelfNo;
    }

    @Override
    public String toString() {
        return "Part{" +
                "model='" + model + '\'' +
                ", partNo='" + partNo + '\'' +
                ", category='" + category + '\'' +
                ", opType=" + opType +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Part part = (Part) o;

        return model != null ? model.equals(part.model) : part.model == null;

    }

    @Override
    public int hashCode() {
        return model != null ? model.hashCode() : 0;
    }
}
