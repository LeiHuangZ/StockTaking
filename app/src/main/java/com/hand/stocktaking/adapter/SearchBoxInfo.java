package com.hand.stocktaking.adapter;

public class SearchBoxInfo {
    private String tags;
    private String sign;
    private String mattertype;
    private String matternum;
    private String mattername;
    private String id;
    private boolean isMatch = false;

    public boolean getIsMatch() {
        return isMatch;
    }

    public void setIsMatch(boolean isMatch) {
        this.isMatch = isMatch;
    }

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

    public String getMattertype() {
        return mattertype;
    }

    public void setMattertype(String mattertype) {
        this.mattertype = mattertype;
    }

    public String getMatternum() {
        return matternum;
    }

    public void setMatternum(String matternum) {
        this.matternum = matternum;
    }

    public String getMattername() {
        return mattername;
    }

    public void setMattername(String mattername) {
        this.mattername = mattername;
    }
}
