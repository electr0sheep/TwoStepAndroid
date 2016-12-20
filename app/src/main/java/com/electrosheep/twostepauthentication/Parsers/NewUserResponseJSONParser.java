package com.electrosheep.twostepauthentication.Parsers;

import com.electrosheep.twostepauthentication.Models.NewUserResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by electrosheep on 12/20/16.
 */

public class NewUserResponseJSONParser {
    public static NewUserResponse parseFeed(String content) {
        NewUserResponse response = new NewUserResponse();
        // first make sure there was a response in the first place
        if (content == null){
            response.setMessage("Cannot contact server");
            response.setResult(false);
            return response;
        }
        try {
            JSONArray ar = new JSONArray(content);

            JSONObject obj = ar.getJSONObject(0);

            response.setMessage(obj.getString("message"));
            response.setResult(obj.getBoolean("result"));

            return response;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}