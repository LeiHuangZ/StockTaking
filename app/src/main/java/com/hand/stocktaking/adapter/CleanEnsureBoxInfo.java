package com.hand.stocktaking.adapter;

public class CleanEnsureBoxInfo {
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

    public String getLastClearTime() {
        return lastClearTime;
    }

    public void setLastClearTime(String lastClearTime) {
        this.lastClearTime = lastClearTime;
    }

    public String getMatternum() {
        return matternum;
    }

    public void setMatternum(String matternum) {
        this.matternum = matternum;
    }

    private String tags = "";
    private String sign = "";
    private String lastClearTime = "";
    private String matternum = "";
}
