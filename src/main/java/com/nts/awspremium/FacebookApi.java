package com.nts.awspremium;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FacebookApi {
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
                try{
                    Long.parseLong(jsonObject.get("id").toString());
                    return jsonObject.get("id").toString();
                }catch (Exception e){
                    return getUID_1(link);
                }
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

    public static boolean getPost(String url) {
        String pattern = "(https://www\\.|https://|www\\.|)facebook\\.com/(?!groups)[^/]+/videos/\\d+";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(url);
        String pattern1 = "https://www\\.facebook\\.com/watch\\?v=\\d+";
        Pattern r1 = Pattern.compile(pattern1);
        Matcher matcher1 = r1.matcher(url);
        String pattern2 = "(https://www\\.|https://|www\\.|)facebook\\.com/(?!groups)[^/]+/posts/.*";
        Pattern r2 = Pattern.compile(pattern2);
        Matcher matcher2 = r2.matcher(url);
        return (matcher.matches()||matcher1.matches()||matcher2.matches());
    }

    public static boolean getVideo(String url) {
        String pattern = "(https://www\\.|https://|www\\.|)facebook\\.com/(?!groups)[^/]+/videos/\\d+";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(url);
        String pattern1 = "https://www\\.facebook\\.com/watch\\?v=\\d+";
        Pattern r1 = Pattern.compile(pattern1);
        Matcher matcher1 = r1.matcher(url);
        return (matcher.matches()||matcher1.matches());
    }
    public static boolean getGroup(String url) {
        String pattern = "(https://www\\.|https://|www\\.|)facebook\\.com/groups/[^/]+/?$";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(url);
        return matcher.matches();
    }

}
