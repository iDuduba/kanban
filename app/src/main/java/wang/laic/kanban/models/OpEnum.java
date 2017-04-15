package wang.laic.kanban.models;

/**
 * Created by duduba on 2017/4/1.
 */

public enum OpEnum {
    OUT("领料出库", 1),
    LOSS("盘亏", 2),
    EXOUT("额外出库",3),
    PROFIT("盘盈", 4),
    TRANSIN("调货入库", 5),
    RETURN("正常退货", 6),
    IN("采购入库", 7),
    TRANSOUT("调货出库", 8),
    RETOUT("供应商退货出库", 9);

    private String name;
    private int type;

    private OpEnum(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public static String getName(int type) {
        for(OpEnum op: OpEnum.values()) {
            if(op.getType() == type) {
                return op.getName();
            }
        }
        return null;
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
