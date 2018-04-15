package com.hand.stocktaking.retrofit;

public class RecordBean {

    /**
     * code : 200
     * message : editOrderinfo-success
     * data : {"project":"为民除害","sourcename":"汉德霍尔科技","loss_rate":"10","lifetime":"10"}
     */

    private String code;
    private String message;
    private DataBean data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
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
         * project : 为民除害
         * sourcename : 汉德霍尔科技
         * loss_rate : 10
         * lifetime : 10
         */

        private String project;
        private String sourcename;
        private String loss_rate;
        private String lifetime;

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public String getSourcename() {
            return sourcename;
        }

        public void setSourcename(String sourcename) {
            this.sourcename = sourcename;
        }

        public String getLoss_rate() {
            return loss_rate;
        }

        public void setLoss_rate(String loss_rate) {
            this.loss_rate = loss_rate;
        }

        public String getLifetime() {
            return lifetime;
        }

        public void setLifetime(String lifetime) {
            this.lifetime = lifetime;
        }
    }
}
