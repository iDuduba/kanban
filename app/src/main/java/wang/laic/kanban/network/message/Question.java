package wang.laic.kanban.network.message;

import com.google.gson.annotations.Expose;

/**
 * Created by duduba on 2017/3/30.
 */

public class Question<T> {
    @Expose
    private int version = 0;

    @Expose
    private int appid;

    @Expose
    private String sign;

    @Expose
    private T body;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getAppid() {
        return appid;
    }

    public void setAppid(int appid) {
        this.appid = appid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
