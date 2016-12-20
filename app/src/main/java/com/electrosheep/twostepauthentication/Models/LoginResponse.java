package com.electrosheep.twostepauthentication.Models;

/**
 * Created by electrosheep on 12/20/16.
 */

public class LoginResponse {

    private String message;
    private boolean result;

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) { this.message = message; }
    public boolean getResult() { return result; };
    public void setResult(boolean result) { this.result = result; }

}