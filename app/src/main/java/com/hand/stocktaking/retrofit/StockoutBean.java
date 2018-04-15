package com.hand.stocktaking.retrofit;

public class StockoutBean {

    /**
     * code : 200
     * message : stockout-success
     * data : {"orderid":"stock03181523377259","is_emptybox":"0","status":"0","send_time":"2018-04-11 00:20:59","send_company":"公司名2","recv_company":"公司名1","trans_company":"公司名2","trans_company_carnum":"fgg","code":"code21021523377259"}
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
         * orderid : stock03181523377259
         * is_emptybox : 0
         * status : 0
         * send_time : 2018-04-11 00:20:59
         * send_company : 公司名2
         * recv_company : 公司名1
         * trans_company : 公司名2
         * trans_company_carnum : fgg
         * code : code21021523377259
         */

        private String orderid;
        private String is_emptybox;
        private String status;
        private String send_time;
        private String send_company;
        private String recv_company;
        private String trans_company;
        private String trans_company_carnum;
        private String code;

        public String getOrderid() {
            return orderid;
        }

        public void setOrderid(String orderid) {
            this.orderid = orderid;
        }

        public String getIs_emptybox() {
            return is_emptybox;
        }

        public void setIs_emptybox(String is_emptybox) {
            this.is_emptybox = is_emptybox;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSend_time() {
            return send_time;
        }

        public void setSend_time(String send_time) {
            this.send_time = send_time;
        }

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

        public String getTrans_company_carnum() {
            return trans_company_carnum;
        }

        public void setTrans_company_carnum(String trans_company_carnum) {
            this.trans_company_carnum = trans_company_carnum;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
