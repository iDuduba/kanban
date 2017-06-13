package wang.laic.kanban.models;

/**
 * Created by duduba on 2017/4/1.
 */


/*
1- 送货上架、2-上架撤回、3-盘盈、4-退回HF、5-无、
6- 领料出库、7-领料退回、8-盘亏、9-额外出库
 */
public enum OpEnum {
    PUTAWAY("送货上架", 1),
    WITHDRAW("上架撤回", 2),
    PROFIT("盘盈", 3),
    HF("退回HF", 4),
    NOTHING("无", 5),
    OUT("领料出库", 6),
    RETURN("领料退回", 7),
    LOSS("盘亏", 8),
    EXOUT("额外出库",9);
//    PROFIT("盘盈", 4),
//    TRANSIN("调货入库", 5),
//    IN("采购入库", 7),
//    TRANSOUT("调货出库", 8),
//    RETOUT("供应商退货出库", 9);

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
