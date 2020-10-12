package com.smurf.app.upgrade;

public class CouponBean {
    /**
     * success : true
     * code : 0
     * message : null
     * data : {"vno":"1026","url":"https://www.langongbao.com/drop/file/doc/apk/V0.1/smurf.apk","isInstallApp":true,"explain":""}
     */

    private boolean success;
    private int code;
    private Object message;
    private DataBean data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * vno : 1026
         * url : https://www.langongbao.com/drop/file/doc/apk/V0.1/smurf.apk
         * isInstallApp : true
         * explain :
         */

        private String vno;
        private String urlX;
        private boolean isInstallAppX;
        private String explain;

        public String getVno() {
            return vno;
        }

        public void setVno(String vno) {
            this.vno = vno;
        }

        public String getUrlX() {
            return urlX;
        }

        public void setUrlX(String urlX) {
            this.urlX = urlX;
        }

        public boolean isIsInstallAppX() {
            return isInstallAppX;
        }

        public void setIsInstallAppX(boolean isInstallAppX) {
            this.isInstallAppX = isInstallAppX;
        }

        public String getExplain() {
            return explain;
        }

        public void setExplain(String explain) {
            this.explain = explain;
        }
    }
}