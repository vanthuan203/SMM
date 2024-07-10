package com.nts.awspremium.system;

import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThreadsApi {
    public static String getUID_1(String link) {

        try {

            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("https://fbuid.mktsoftware.net/api/v1/fbprofile?url=" + link).get().build();

            Response response = client.newCall(request).execute();

            String resultJson = response.body().string();
            response.body().close();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            if(jsonObject.get("uid")==null){
                return null;
            }else{
                return jsonObject.get("uid").toString();
            }

        } catch (Exception e) {
            return null;
        }
    }
    public static String getUID_2(String link) {

        try {

            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("https://ffb.vn/api/tool/get-id-fb?idfb=" + link).get().build();

            Response response = client.newCall(request).execute();

            String resultJson = response.body().string();
            response.body().close();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            if(jsonObject.get("id")==null){
                    return getUID_1(link);
            }else{
                return jsonObject.get("id").toString();
            }

        } catch (Exception e) {
            return null;
        }
    }


    public static String getUID_3(String link) {

        try {
            JSONObject json = new JSONObject();
            json.put("link", link);
            // Convert JSON object to RequestBody
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, String.valueOf(json));
            Request request = new Request.Builder()
                    .url("https://fchat-app.salekit.com:4039/api/v1/facebook/get_uid")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            if(jsonObject.get("uid")==null){
                return getUID_2(link);
            }else{
                return jsonObject.get("uid").toString();
            }
        } catch (Exception e) {
            return null;
        }
    }


    public static String getThreadsId(String url) {
        String regex = "threads\\.net/@([^/]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    public static String getPostId(String url) {
        String regex = "threads\\.net/@[^/]+/post/([^/]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


}
