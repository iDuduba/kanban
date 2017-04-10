package wang.laic.kanban.models;

/**
 * Created by duduba on 2017/4/1.
 */

public enum OrderStatusEnum {
    ALL("全部", 0),
    WAY("在途", 1),
    ARRIVAL("到货",2),
    REVOKE("撤销", 3);


    private String name;
    private int type;

    private OrderStatusEnum(String name, int type) {
        this.name = name;
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

    public static OrderStatusEnum valueOf(int type) {
        switch (type) {
            case 0:
                return ALL;
            case 1:
                return WAY;
            case 2:
                return ARRIVAL;
            case 3:
                return REVOKE;
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

    @Override
    public String toString() {
        return name;
    }
}
