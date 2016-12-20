package com.electrosheep.twostepauthentication.Models;

public class NewUserResponse {
    private String message;
    private boolean result;

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) { this.message = message; }
    public boolean getResult() { return result; };
    public void setResult(boolean result) { this.result = result; }
}
