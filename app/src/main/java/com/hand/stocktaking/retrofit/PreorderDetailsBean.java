package com.hand.stocktaking.retrofit;

import java.util.List;

public class PreorderDetailsBean {

    /**
     * code : 200
     * message : preorder-success
     * data : {"id":"10","buyorderid":"stockbuy1522851039","pretime":"2018-04-04 12:00:00","sourcename":"启动","buytime":null,"project":null,"loss_rate":null,"lifetime":null,"piao":null,"boxnum":[{"id":"22","buyorder_id":"stockbuy1522851039","box_sign":"000007","box_num":"10","box_price":"2.30","box_type":"1","tags":["E000000000000000000000E020","E000000000000000000000E021","E000000000000000000000E022","E000000000000000000000E023","E000000000000000000000E024","E000000000000000000000E025","E000000000000000000000E026","E000000000000000000000E027","E000000000000000000000E028","E000000000000000000000E029"]},{"id":"23","buyorder_id":"stockbuy1522851039","box_sign":"000008","box_num":"5","box_price":"2.10","box_type":"1","tags":["E000000000000000000000E030","E000000000000000000000E031","E000000000000000000000E032","E000000000000000000000E033","E000000000000000000000E034"]}],"buyusername":"测试1","buycompanyname":"公司名2"}
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
         * id : 10
         * buyorderid : stockbuy1522851039
         * pretime : 2018-04-04 12:00:00
         * sourcename : 启动
         * buytime : null
         * project : null
         * loss_rate : null
         * lifetime : null
         * piao : null
         * boxnum : [{"id":"22","buyorder_id":"stockbuy1522851039","box_sign":"000007","box_num":"10","box_price":"2.30","box_type":"1","tags":["E000000000000000000000E020","E000000000000000000000E021","E000000000000000000000E022","E000000000000000000000E023","E000000000000000000000E024","E000000000000000000000E025","E000000000000000000000E026","E000000000000000000000E027","E000000000000000000000E028","E000000000000000000000E029"]},{"id":"23","buyorder_id":"stockbuy1522851039","box_sign":"000008","box_num":"5","box_price":"2.10","box_type":"1","tags":["E000000000000000000000E030","E000000000000000000000E031","E000000000000000000000E032","E000000000000000000000E033","E000000000000000000000E034"]}]
         * buyusername : 测试1
         * buycompanyname : 公司名2
         */

        private String id;
        private String buyorderid;
        private String pretime;
        private String sourcename;
        private Object buytime;
        private Object project;
        private Object loss_rate;
        private Object lifetime;
        private Object piao;
        private String buyusername;
        private String buycompanyname;
        private List<BoxnumBean> boxnum;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBuyorderid() {
            return buyorderid;
        }

        public void setBuyorderid(String buyorderid) {
            this.buyorderid = buyorderid;
        }

        public String getPretime() {
            return pretime;
        }

        public void setPretime(String pretime) {
            this.pretime = pretime;
        }

        public String getSourcename() {
            return sourcename;
        }

        public void setSourcename(String sourcename) {
            this.sourcename = sourcename;
        }

        public Object getBuytime() {
            return buytime;
        }

        public void setBuytime(Object buytime) {
            this.buytime = buytime;
        }

        public Object getProject() {
            return project;
        }

        public void setProject(Object project) {
            this.project = project;
        }

        public Object getLoss_rate() {
            return loss_rate;
        }

        public void setLoss_rate(Object loss_rate) {
            this.loss_rate = loss_rate;
        }

        public Object getLifetime() {
            return lifetime;
        }

        public void setLifetime(Object lifetime) {
            this.lifetime = lifetime;
        }

        public Object getPiao() {
            return piao;
        }

        public void setPiao(Object piao) {
            this.piao = piao;
        }

        public String getBuyusername() {
            return buyusername;
        }

        public void setBuyusername(String buyusername) {
            this.buyusername = buyusername;
        }

        public String getBuycompanyname() {
            return buycompanyname;
        }

        public void setBuycompanyname(String buycompanyname) {
            this.buycompanyname = buycompanyname;
        }

        public List<BoxnumBean> getBoxnum() {
            return boxnum;
        }

        public void setBoxnum(List<BoxnumBean> boxnum) {
            this.boxnum = boxnum;
        }

        public static class BoxnumBean {
            /**
             * id : 22
             * buyorder_id : stockbuy1522851039
             * box_sign : 000007
             * box_num : 10
             * box_price : 2.30
             * box_type : 1
             * tags : ["E000000000000000000000E020","E000000000000000000000E021","E000000000000000000000E022","E000000000000000000000E023","E000000000000000000000E024","E000000000000000000000E025","E000000000000000000000E026","E000000000000000000000E027","E000000000000000000000E028","E000000000000000000000E029"]
             */

            private String id;
            private String buyorder_id;
            private String box_sign;
            private String box_num;
            private String box_price;
            private String box_type;
            private List<String> tags;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getBuyorder_id() {
                return buyorder_id;
            }

            public void setBuyorder_id(String buyorder_id) {
                this.buyorder_id = buyorder_id;
            }

            public String getBox_sign() {
                return box_sign;
            }

            public void setBox_sign(String box_sign) {
                this.box_sign = box_sign;
            }

            public String getBox_num() {
                return box_num;
            }

            public void setBox_num(String box_num) {
                this.box_num = box_num;
            }

            public String getBox_price() {
                return box_price;
            }

            public void setBox_price(String box_price) {
                this.box_price = box_price;
            }

            public String getBox_type() {
                return box_type;
            }

            public void setBox_type(String box_type) {
                this.box_type = box_type;
            }

            public List<String> getTags() {
                return tags;
            }

            public void setTags(List<String> tags) {
                this.tags = tags;
            }
        }
    }
}
