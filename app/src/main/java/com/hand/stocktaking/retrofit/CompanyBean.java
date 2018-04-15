package com.hand.stocktaking.retrofit;

import java.util.List;

public class CompanyBean {

    /**
     * code : 200
     * message : getCompanyname-success
     * data : [{"id":"1","name":"公司名1"},{"id":"2","name":"公司名2"},{"id":"3","name":"公司名3"},{"id":"4","name":"公司名4"},{"id":"6","name":"公司名5"},{"id":"8","name":"玩玩"}]
     */

    private String code;
    private String message;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 1
         * name : 公司名1
         */

        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
