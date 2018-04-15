package com.hand.stocktaking.adapter;

public class DoorBoxInfo {
    private String rfid = "";
    private String boxType = "";
    private String sendCompany = "";
    private String receiveCompany = "";
    private String sendTime = "";
    private boolean isRedundant = false;

    public boolean isRedundant() {
        return isRedundant;
    }

    public void setRedundant(boolean redundant) {
        isRedundant = redundant;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getBoxType() {
        return boxType;
    }

    public void setBoxType(String boxType) {
        this.boxType = boxType;
    }

    public String getSendCompany() {
        return sendCompany;
    }

    public void setSendCompany(String sendCompany) {
        this.sendCompany = sendCompany;
    }

    public String getReceiveCompany() {
        return receiveCompany;
    }

    public void setReceiveCompany(String receiveCompany) {
        this.receiveCompany = receiveCompany;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }
}
