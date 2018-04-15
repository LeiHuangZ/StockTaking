package com.hand.stocktaking.adapter;

public class EntryBoxInfo {
    private String tags;
    private String sign;
    private String mattertype;
    private String matternum;
    private String mattername;
    private String id;
    private boolean isChecked = false;

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
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
