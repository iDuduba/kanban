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
    private String category;

    @Expose(deserialize = false,serialize = true)
    private int opType = OpEnum.OUT.getType();

    @Expose
    private int quantity = 0;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getOpType() {
        return opType;
    }

    public void setOpType(int opType) {
        this.opType = opType;
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
}
