package com.hand.stocktaking.adapter;

public class ReceiveBoxInfo {
    private String rfid = "";
    private String boxType = "";
    private String sendCompany = "";
    private String receiveCompany = "";
    private String sendTime = "";
    private String operation = "";
    private boolean isResolved = false;

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
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
