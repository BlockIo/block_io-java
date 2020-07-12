package com.blockio.lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class JsonUtils {

    public static Map<String, Object> parseJson(String json){
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    public static boolean isJson(String Json) {
        Gson gson = new Gson();
        try {
            gson.fromJson(Json, Object.class);
            Object jsonObjType = gson.fromJson(Json, Object.class).getClass();
            if(jsonObjType.equals(String.class)){
                return false;
            }
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }
}
