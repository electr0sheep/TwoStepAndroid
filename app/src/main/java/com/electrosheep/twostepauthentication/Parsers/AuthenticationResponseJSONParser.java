package com.electrosheep.twostepauthentication.Parsers;

import com.electrosheep.twostepauthentication.Models.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by electrosheep on 12/21/16.
 */

public class AuthenticationResponseJSONParser {
    public static AuthenticationResponse parseFeed(String content) {
        AuthenticationResponse response = new AuthenticationResponse();
        // first make sure there was a response in the first place
        if (content == null){
            response.setMessage("Cannot contact server");
            response.setResult(false);
            return response;
        }
        try {
            JSONObject obj = new JSONObject(content);

            response.setMessage(obj.getString("message"));
            response.setResult(obj.getBoolean("result"));

            return response;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
