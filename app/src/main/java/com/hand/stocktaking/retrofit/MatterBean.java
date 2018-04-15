package com.hand.stocktaking.retrofit;

import java.util.List;

public class MatterBean {

    /**
     * code : 200
     * message : tagsTobox-success
     * data : [{"id":"305","tags":"E0000000000000000000E194","sign":"0000011","mattertype":null,"matternum":"","mattername":null}]
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
         * id : 305
         * tags : E0000000000000000000E194
         * sign : 0000011
         * mattertype : null
         * matternum :
         * mattername : null
         */

        private String id;
        private String tags;
        private String sign;
        private Object mattertype;
        private String matternum;
        private Object mattername;

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

        public Object getMattertype() {
            return mattertype;
        }

        public void setMattertype(Object mattertype) {
            this.mattertype = mattertype;
        }

        public String getMatternum() {
            return matternum;
        }

        public void setMatternum(String matternum) {
            this.matternum = matternum;
        }

        public Object getMattername() {
            return mattername;
        }

        public void setMattername(Object mattername) {
            this.mattername = mattername;
        }
    }
}
