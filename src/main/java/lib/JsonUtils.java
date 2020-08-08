package lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

public class JsonUtils {

    public static Map<String, Object> parseJson(String json){
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return new Gson().fromJson(json, type);
    }
}
