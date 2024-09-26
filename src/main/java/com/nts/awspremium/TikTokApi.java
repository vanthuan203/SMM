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
import java.net.*;
import java.net.Authenticator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TikTokApi {

    public static Integer getFollowerCount(String tiktok_id,Integer index) {

        try {
            if(index<=0){
                return -2;
            }
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("https://countik.com/api/exist/" + tiktok_id).get().build();

            Response response = client.newCall(request).execute();

            String resultJson = response.body().string();
            System.out.println(resultJson);
            response.body().close();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);
            if(jsonObject.get("sec_uid")==null){
                return getFollowerCount(tiktok_id,index-1);
            }else{
                request = new Request.Builder().url("https://countik.com/api/userinfo?sec_user_id=" + jsonObject.get("sec_uid").toString()).get().build();

                response = client.newCall(request).execute();

                resultJson = response.body().string();
                obj = new JSONParser().parse(resultJson);
                jsonObject = (JSONObject) obj;
                System.out.println(jsonObject);
                if(jsonObject.get("followerCount")==null){
                    return -2;
                }
                return Integer.parseInt(jsonObject.get("followerCount").toString());
            }

        } catch (Exception e) {
            return -2;
        }
    }

    public static JSONObject getInfoVideoTikTok(String link,Integer index) {
        try {
            JSONObject json = new JSONObject();
            if(index<=0){
                json.put("status","error");
                return json;
            }
            json.put("url", link);
            // Convert JSON object to RequestBody
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, String.valueOf(json));
            Request request = new Request.Builder()
                    .url("https://countik.com/api/video/exist")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            if(jsonObject.get("status").equals("error"))
            {
                getInfoVideoTikTok(link,index-1);
            }
            return jsonObject;
        } catch (Exception e) {
            JSONObject resp=new JSONObject();
            resp.put("status","error");
            return resp;
        }
    }

    public static Integer getCountLike(String video_id) {

        try {
            JSONObject json = new JSONObject();
            String link="https://www.tiktok.com/@/video/"+video_id;
            json.put("url", link);
            // Convert JSON object to RequestBody
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, String.valueOf(json));
            Request request = new Request.Builder()
                    .url("https://countik.com/api/video/exist")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            if(jsonObject.get("status").toString().equals("error")){
                return -2;
            }else{
                return Integer.parseInt(jsonObject.get("likes").toString());
            }
        } catch (Exception e) {
            return -2;
        }
    }

    public static Integer getCountView(String video_id) {

        try {
            JSONObject json = new JSONObject();
            String link="https://www.tiktok.com/@/video/"+video_id;
            json.put("url", link);
            // Convert JSON object to RequestBody
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, String.valueOf(json));
            Request request = new Request.Builder()
                    .url("https://countik.com/api/video/exist")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            if(jsonObject.get("status").toString().equals("error")){
                return -2;
            }else{
                return Integer.parseInt(jsonObject.get("plays").toString());
            }
        } catch (Exception e) {
            return -2;
        }
    }
    public static Integer getCountComment(String video_id) {

        try {
            JSONObject json = new JSONObject();
            String link="https://www.tiktok.com/@/video/"+video_id;
            json.put("url", link);
            // Convert JSON object to RequestBody
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, String.valueOf(json));
            Request request = new Request.Builder()
                    .url("https://countik.com/api/video/exist")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            if(jsonObject.get("status").toString().equals("error")){
                return -2;
            }else{
                return Integer.parseInt(jsonObject.get("comments").toString());
            }
        } catch (Exception e) {
            return -2;
        }
    }
    public static String getTiktokId(String url) {
        String pattern = "tiktok\\.com\\/@([a-zA-Z0-9_.]+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(url);
        if (m.find()) {
            return "@" + m.group(1);
        } else {
            return null;
        }
    }
    public static String checkTiktokTrue(String url) {
        try {
            // Kết nối tới trang YouTube và lấy nội dung trang
            Document doc = Jsoup.connect(url).get();
            // Tìm tất cả các thẻ <script> chứa đoạn JSON
            Elements scriptElements = doc.select("script");
            for (Element scriptElement : scriptElements) {
                String scriptContent = scriptElement.html();
                if (scriptContent.contains("responseContext")) {
                    // Lấy phần JSON trong nội dung của thẻ script
                    int startIndex = scriptContent.indexOf("{");
                    int endIndex = scriptContent.lastIndexOf("}") + 1;
                    String jsonString = scriptContent.substring(startIndex, endIndex);
                    //System.out.println(jsonString);
                    JsonReader reader = new JsonReader(new StringReader(jsonString));
                    reader.setLenient(true);
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    JsonObject jsonObject =  jsonElement.getAsJsonObject();
                    JsonObject jsonElement11 =  jsonObject.getAsJsonObject("metadata");
                    String id = jsonObject.getAsJsonObject("metadata")
                            .getAsJsonObject("channelMetadataRenderer")
                            .get("title").toString().replace("\"","");
                    String chann = jsonObject.getAsJsonObject("metadata")
                            .getAsJsonObject("channelMetadataRenderer")
                            .get("externalId").toString().replace("\"","");
                    return id+","+chann;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    public static String getVideoId(String url) {
        String pattern = "tiktok\\.com/.*/video/(\\d+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(url);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    public static JSONObject checkAccountTiktok(String link) {

        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("https://api.like3s.vn/api/extension/find-uid?link=" + link).get().build();

            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject;

        } catch (Exception e) {
            return null;
        }
    }

    public static String getIdShare(String url) {
        String regex = "https://vt.tiktok.com/(\\w+)/";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
