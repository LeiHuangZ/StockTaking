package com.hand.stocktaking.retrofit;

import java.util.List;

/**
 * @author huang
 * 登陆用户的信息实体类
 */
public class UserBean {
    @Override
    public String toString() {
        return "UserBean{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    /**
     * code : 200
     * message : login-success
     * data : [{"id":"2","jobnumber":"000001","mobile":"13022222222","username":"测试2","email":"12222@qq.com","company_id":"2","type":"1","status":"1","company_name":"公司名2","company_type":"0"}]
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
        @Override
        public String toString() {
            return "DataBean{" +
                    "id='" + id + '\'' +
                    ", jobnumber='" + jobnumber + '\'' +
                    ", mobile='" + mobile + '\'' +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", company_id='" + company_id + '\'' +
                    ", type='" + type + '\'' +
                    ", status='" + status + '\'' +
                    ", company_name='" + company_name + '\'' +
                    ", company_type='" + company_type + '\'' +
                    '}';
        }

        /**
         * id : 2
         * jobnumber : 000001
         * mobile : 13022222222
         * username : 测试2
         * email : 12222@qq.com
         * company_id : 2
         * type : 1
         * status : 1
         * company_name : 公司名2
         * company_type : 0
         */

        private String id;
        private String jobnumber;
        private String mobile;
        private String username;
        private String email;
        private String company_id;
        private String type;
        private String status;
        private String company_name;
        private String company_type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getJobnumber() {
            return jobnumber;
        }

        public void setJobnumber(String jobnumber) {
            this.jobnumber = jobnumber;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCompany_id() {
            return company_id;
        }

        public void setCompany_id(String company_id) {
            this.company_id = company_id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCompany_name() {
            return company_name;
        }

        public void setCompany_name(String company_name) {
            this.company_name = company_name;
        }

        public String getCompany_type() {
            return company_type;
        }

        public void setCompany_type(String company_type) {
            this.company_type = company_type;
        }
    }
}
