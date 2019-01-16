package com.example.uibestpractice.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseJSON {

    public static String parseJSONWithJSONObject(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            return jsonObject.getString("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
