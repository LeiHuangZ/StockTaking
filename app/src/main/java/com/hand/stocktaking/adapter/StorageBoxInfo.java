package com.hand.stocktaking.adapter;

public class StorageBoxInfo {
    private String rfid;
    private String buyCompany;
    private String boxType;
    private String boxPrice;
    private String boxNum;
    private String preTime;
    private boolean isResolved = false;

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getBuyCompany() {
        return buyCompany;
    }

    public void setBuyCompany(String buyCompany) {
        this.buyCompany = buyCompany;
    }

    public String getBoxType() {
        return boxType;
    }

    public void setBoxType(String boxType) {
        this.boxType = boxType;
    }

    public String getBoxPrice() {
        return boxPrice;
    }

    public void setBoxPrice(String boxPrice) {
        this.boxPrice = boxPrice;
    }

    public String getBoxNum() {
        return boxNum;
    }

    public void setBoxNum(String boxNum) {
        this.boxNum = boxNum;
    }

    public String getPreTime() {
        return preTime;
    }

    public void setPreTime(String preTime) {
        this.preTime = preTime;
    }
}
