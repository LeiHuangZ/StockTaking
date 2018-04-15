package com.hand.stocktaking.adapter;

public class OutDialogBoxInfo {
    private String boxNum;
    private String boxType;

    public String getRfid() {
        return boxNum;
    }

    public void setRfid(String rfid) {
        this.boxNum = rfid;
    }


    public String getBoxType() {
        return boxType;
    }

    public void setBoxType(String boxType) {
        this.boxType = boxType;
    }

}
