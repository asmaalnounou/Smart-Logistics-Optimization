package com.example.lab4;
public class userInfo {
    private String fullname;
    private String userEmail;
    private String ID;
    private String phoneNum;

    public userInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(userInfo.class)
    }

    public String getName() {
        return fullname;
    }

    public void setName(String fullname) {
        this.fullname = fullname;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    public String getID(){
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPhoneNum(){
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum){
        this.phoneNum = phoneNum;
    }
}