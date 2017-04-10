package wang.laic.kanban.network.message;

import com.google.gson.annotations.Expose;

/**
 * Created by duduba on 2017/3/31.
 */

public class Answer<T> {
    @Expose
    private int code = 0;

    @Expose
    private String message;

    @Expose
    private T body;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
