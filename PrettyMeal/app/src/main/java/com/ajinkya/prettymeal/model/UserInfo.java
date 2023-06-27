package com.ajinkya.prettymeal.model;

public class UserInfo {
    public int UserId;
    public String UserName;
    public String UserEmail;
    public String UserMobileNo;
    public String UserPassword;
    public String LocationLat;
    public String LocationLng;
    public String UserAddress;
    public String UserMembership;
    public String UserProfileUrl;
    public String history_TransactionNo;
    public String history_DateTime;

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int UserId) {
        UserId = UserId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserEmail() {
        return UserEmail;
    }

    public void setUserEmail(String userEmail) {
        UserEmail = userEmail;
    }

    public String getUserMobileNo() {
        return UserMobileNo;
    }

    public void setUserMobileNo(String userMobileNo) {
        UserMobileNo = userMobileNo;
    }

    public String getUserPassword() {
        return UserPassword;
    }

    public void setUserPassword(String userPassword) {
        UserPassword = userPassword;
    }

    public String getLocationLat() {
        return LocationLat;
    }

    public void setLocationLat(String locationLat) {
        LocationLat = locationLat;
    }

    public String getLocationLng() {
        return LocationLng;
    }

    public void setLocationLng(String locationLng) {
        LocationLng = locationLng;
    }

    public String getUserAddress() {
        return UserAddress;
    }

    public void setUserAddress(String userAddress) {
        UserAddress = userAddress;
    }

    public String getUserMembership() {
        return UserMembership;
    }

    public void setUserMembership(String userMembership) {
        UserMembership = userMembership;
    }

    public String getUserProfileUrl() {
        return UserProfileUrl;
    }

    public void setUserProfileUrl(String userProfileUrl) {
        UserProfileUrl = userProfileUrl;
    }

    public String toString(String table){
        String s = "";
        switch (table){
            case "BusinessHistory":
                s = "BusinessHistory: id: "+UserId+",UserName: "+UserName
                        +",history_DateTime: "+history_DateTime+",history_TransactionNo: "+history_TransactionNo
                        +"\n";
                break;
            case "CustomerHistory":
                s = "CustomerHistory: id: "+UserId+",UserName: "+UserName
                        +",history_DateTime: "+history_DateTime+",history_TransactionNo: "+history_TransactionNo
                        +"\n";
                break;
            case "Customer":
                s = "Customer: id: "+UserId+",UserName: "+UserName
                        +",UserEmail: "+UserEmail+",UserMobileNo: "+UserMobileNo
                        +",UserPassword: "+UserPassword+",UserAddress: "+UserAddress
                        +"\n";
                break;
            case "Business":
                s = "Business: id: "+UserId+",UserName: "+UserName
                        +",UserEmail: "+UserEmail+",UserMobileNo: "+UserMobileNo
                        +",UserPassword: "+UserPassword+",UserAddress: "+UserAddress
                        +"\n";
                break;
            default:
                break;
        }
        return s;
    }
}