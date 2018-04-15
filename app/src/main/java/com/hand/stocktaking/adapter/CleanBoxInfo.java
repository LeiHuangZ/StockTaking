package com.hand.stocktaking.adapter;

public class CleanBoxInfo {
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

    public String getCleaner() {
        return cleaner;
    }

    public void setCleaner(String cleaner) {
        this.cleaner = cleaner;
    }

    public String getCleanTime() {
        return cleanTime;
    }

    public void setCleanTime(String cleanTime) {
        this.cleanTime = cleanTime;
    }

    private String tags = "";
    private String sign = "";
    private String cleaner = "";
    private String cleanTime = "";
}
