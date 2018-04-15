package com.hand.stocktaking.retrofit;

import java.util.List;

/**
 * @author huang
 * 获取预定订单号的实体类
 */
public class PreorderBean {

    /**
     * code : 200
     * message : getPreorder-success
     * data : [{"buyorderid":"aaa_aaaaa007"},{"buyorderid":"stockbuy1522851039"},{"buyorderid":"stockbuy1522851071"},{"buyorderid":"stockbuy1522851081"},{"buyorderid":"stockbuy1522851092"}]
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
         * buyorderid : aaa_aaaaa007
         */

        private String buyorderid;

        public String getBuyorderid() {
            return buyorderid;
        }

        public void setBuyorderid(String buyorderid) {
            this.buyorderid = buyorderid;
        }
    }
}
