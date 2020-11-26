package com.fadada.faceverifysdk.bean;

public class ResponseData {
    private String msg;

    private boolean isSuccess;

    private String code;

    private Sign data;

    public String getMsg() {
        return msg;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getCode() {
        return code;
    }

    public Sign getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "msg='" + msg + '\'' +
                ", isSuccess=" + isSuccess +
                ", code='" + code + '\'' +
                ", data=" + data.toString() +
                '}';
    }
}
