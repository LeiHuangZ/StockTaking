package com.hand.stocktaking.retrofit;

import java.util.List;

public class DoorBoxsBean {

    /**
     * code : 200
     * message : codeDoororderinfo-success
     * data : {"send_company":"公司名2","recv_company":"公司名1","trans_company":"运输公司11111111111","boxtotal":2,"is_emptybox":"0","orderid":"stock24031523429361","trans_company_carnum":"ggg","send_time":"2018-04-11 14:49:21","boxs":[{"id":"299","tags":"E202","sign":"unknown"},{"id":"302","tags":"22558585857585858585","sign":"unknown"}]}
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
         * send_company : 公司名2
         * recv_company : 公司名1
         * trans_company : 运输公司11111111111
         * boxtotal : 2
         * is_emptybox : 0
         * orderid : stock24031523429361
         * trans_company_carnum : ggg
         * send_time : 2018-04-11 14:49:21
         * boxs : [{"id":"299","tags":"E202","sign":"unknown"},{"id":"302","tags":"22558585857585858585","sign":"unknown"}]
         */

        private String send_company;
        private String recv_company;
        private String trans_company;
        private int boxtotal;
        private String is_emptybox;
        private String orderid;
        private String trans_company_carnum;
        private String send_time;
        private List<BoxsBean> boxs;

        public String getSend_company() {
            return send_company;
        }

        public void setSend_company(String send_company) {
            this.send_company = send_company;
        }

        public String getRecv_company() {
            return recv_company;
        }

        public void setRecv_company(String recv_company) {
            this.recv_company = recv_company;
        }

        public String getTrans_company() {
            return trans_company;
        }

        public void setTrans_company(String trans_company) {
            this.trans_company = trans_company;
        }

        public int getBoxtotal() {
            return boxtotal;
        }

        public void setBoxtotal(int boxtotal) {
            this.boxtotal = boxtotal;
        }

        public String getIs_emptybox() {
            return is_emptybox;
        }

        public void setIs_emptybox(String is_emptybox) {
            this.is_emptybox = is_emptybox;
        }

        public String getOrderid() {
            return orderid;
        }

        public void setOrderid(String orderid) {
            this.orderid = orderid;
        }

        public String getTrans_company_carnum() {
            return trans_company_carnum;
        }

        public void setTrans_company_carnum(String trans_company_carnum) {
            this.trans_company_carnum = trans_company_carnum;
        }

        public String getSend_time() {
            return send_time;
        }

        public void setSend_time(String send_time) {
            this.send_time = send_time;
        }

        public List<BoxsBean> getBoxs() {
            return boxs;
        }

        public void setBoxs(List<BoxsBean> boxs) {
            this.boxs = boxs;
        }

        public static class BoxsBean {
            /**
             * id : 299
             * tags : E202
             * sign : unknown
             */

            private String id;
            private String tags;
            private String sign;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getTags() {
                return tags;
            }

            public void setTags(String tags) {
                this.tags = tags;
            }

            public String getSign() {
                return sign;
            }

            public void setSign(String sign) {
                this.sign = sign;
            }
        }
    }
}
