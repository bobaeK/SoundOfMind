package com.example.admin.stt_phone;

public class RowModel {
    String nameText, phoneNumber;

    public RowModel(String nameText, String phoneNumber) {
        this.nameText = nameText;
        this.phoneNumber = phoneNumber;
    }

    public String getMainText() {
        return nameText;
    }

    public void setMainText(String nameText) {
        this.nameText = nameText;
    }

    public String getSubText() {
        return phoneNumber;
    }

    public void setSubText(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}