package wang.laic.kanban.network.message;

/**
 * Created by duduba on 2017/4/3.
 */

public class Failure extends Answer<String> {
    private String extra;

    public Failure() {
        setCode(7777);
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
