package com.fadada.faceverifysdk.bean;

public class Sign {
    private String sign;

    private String orderId;

    private String name;

    private String idcard;

    private String h5faceId;

    private String webankAppId;

    public String getSign() {
        return sign;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getName() {
        return name;
    }

    public String getIdcard() {
        return idcard;
    }

    public String getH5faceId() {
        return h5faceId;
    }

    public String getWebankAppId() {
        return webankAppId;
    }

    @Override
    public String toString() {
        return "Sign{" +
                "sign='" + sign + '\'' +
                ", orderId='" + orderId + '\'' +
                ", name='" + name + '\'' +
                ", idcard='" + idcard + '\'' +
                ", h5faceId='" + h5faceId + '\'' +
                ", webankAppId='" + webankAppId + '\'' +
                '}';
    }
}
