package wang.laic.kanban.models;

import com.google.gson.annotations.Expose;

/**
 * Created by duduba on 2017/3/31.
 */

public class Part {

    @Expose
    private String model;

    @Expose(deserialize = true,serialize = false)
    private String partNo;

    @Expose(deserialize = true,serialize = false)
    private String category;

    @Expose(deserialize = false,serialize = true)
    private int opType = OpEnum.OUT.getType();

    @Expose
    private int quantity;

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
