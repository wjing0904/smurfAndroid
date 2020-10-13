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
    private String message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
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
        private String url;
        private boolean isInstallApp;
        private String explain;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isInstallApp() {
            return isInstallApp;
        }

        public void setInstallApp(boolean installApp) {
            isInstallApp = installApp;
        }

        public String getVno() {
            return vno;
        }

        public void setVno(String vno) {
            this.vno = vno;
        }


        public String getExplain() {
            return explain;
        }

        public void setExplain(String explain) {
            this.explain = explain;
        }
    }
}
