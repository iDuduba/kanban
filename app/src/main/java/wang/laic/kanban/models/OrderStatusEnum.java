package wang.laic.kanban.models;

/**
 * Created by duduba on 2017/4/1.
 */

public enum OrderStatusEnum {
    ALL("全部", "全部", 1111),
    WAY("在途", "入库", 0),
    AOG("到货", "到货", 1),
    REVOKED("撤销", "撤销", 9);


    private String name;
    private String backName;
    private int type;

    private OrderStatusEnum(String name, String backName, int type) {
        this.name = name;
        this.backName = backName;
        this.type = type;
    }

    public static String getName(int type) {
        for(OrderStatusEnum op: OrderStatusEnum.values()) {
            if(op.getType() == type) {
                return op.getName();
            }
        }
        return null;
    }
    public static String getBackName(int type) {
        for(OrderStatusEnum op: OrderStatusEnum.values()) {
            if(op.getType() == type) {
                return op.getBackName();
            }
        }
        return null;
    }

    public static OrderStatusEnum valueOf(int type) {
        switch (type) {
            case 1111:
                return ALL;
            case 0:
                return WAY;
            case 1:
                return AOG;
            case 9:
                return REVOKED;
            default:
                return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBackName() {
        return backName;
    }

    public void setBackName(String backName) {
        this.backName = backName;
    }

    @Override
    public String toString() {
        return name;
    }
}
