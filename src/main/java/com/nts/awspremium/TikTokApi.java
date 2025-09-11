package com.nts.awspremium;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.nts.awspremium.repositories.AccountCloneRepository;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.*;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class TikTokApi {
    public static Integer getFollowerCountOFF(String tiktok_id,Integer index) {

        try {
            if(index<=0){
                return -2;
            }
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("trong1-1.byeip.net", 16000));
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("https://countik.com/api/exist/" + tiktok_id).get().build();

            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            //System.out.println(jsonObject);
            if(jsonObject.get("sec_uid")==null){
                return getFollowerCount(tiktok_id,index-1);
            }else{
                request = new Request.Builder().url("https://countik.com/api/userinfo?sec_user_id=" + jsonObject.get("sec_uid").toString()).get().build();

                response = client.newCall(request).execute();

                resultJson = response.body().string();
                obj = new JSONParser().parse(resultJson);
                jsonObject = (JSONObject) obj;
                //System.out.println(jsonObject);
                if(jsonObject.get("followerCount")==null){
                    return -2;
                }
                return Integer.parseInt(jsonObject.get("followerCount").toString());
            }

        } catch (Exception e) {
            return -2;
        }
    }


    public static String getKey() {

        try {
            Random random = new Random();
            return random.nextInt(10)<=5?"199c73e9e3msh0138b95e7755c65p1c4d6ajsn9330b60a69c8":"4010c38bfamsh398346af7e9f654p1492c2jsn20af8f084b5a";

        } catch (Exception e) {
            return "4010c38bfamsh398346af7e9f654p1492c2jsn20af8f084b5a";
        }
    }



    public static Integer getFollowerCount(String tiktok_id,Integer index) {

        try {
            if(index<=0){
                return -2;
            }
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/info?unique_id="+tiktok_id)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                int followerCount = jsonObject
                        .getAsJsonObject("data")
                        .getAsJsonObject("stats")
                        .get("followerCount")
                        .getAsInt();
                return followerCount;
            }else if (jsonObject.get("msg").getAsString().contains("unique_id is invalid")) {
                // Lấy followerCount từ data.stats
              return -1;
            }else{
                getFollowerCount(tiktok_id,index-1);
            }
        } catch (Exception e) {
            return -2;
        }
        return -2;
    }

    public static Integer checkAccount(String tiktok_id,Integer index) {

        try {
            if(index<=0){
                return -2;
            }
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/info?unique_id="+tiktok_id)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                Boolean check = jsonObject
                        .getAsJsonObject("data")
                        .getAsJsonObject("user")
                        .getAsJsonObject("general_permission").isJsonNull();
                if(check){
                    return 1;
                }else{
                    return -1;
                }

            }else if (jsonObject.get("msg").getAsString().contains("unique_id is invalid")) {
                // Lấy followerCount từ data.stats
                return -1;
            }else{
                getFollowerCount(tiktok_id,index-1);
            }
        } catch (Exception e) {
            return -2;
        }
        return -2;
    }

    public static Integer getFollowerCountByUserId(String user_id,Integer index) {

        try {
            if(index<=0){
                return -2;
            }
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/info?user_id="+user_id)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                int followerCount = jsonObject
                        .getAsJsonObject("data")
                        .getAsJsonObject("stats")
                        .get("followerCount")
                        .getAsInt();
                return followerCount;
            }else{
                getFollowerCountByUserId(user_id,index-1);
            }
        } catch (Exception e) {
            return -2;
        }
        return -2;
    }

    public static Integer getVideoCount(String tiktok_id,Integer index) {

        try {
            if(index<=0){
                return -2;
            }
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/info?unique_id="+tiktok_id)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                int videoCount = jsonObject
                        .getAsJsonObject("data")
                        .getAsJsonObject("stats")
                        .get("videoCount")
                        .getAsInt();
                return videoCount;
            }else if (jsonObject.get("msg").getAsString().contains("unique_id is invalid")) {
                // Lấy videoCount từ data.stats
                return -1;
            }else{
                getVideoCount(tiktok_id,index-1);
            }
        } catch (Exception e) {
            return -2;
        }
        return -2;
    }

    public static Integer getFollowingCount(String tiktok_id) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/info?unique_id="+tiktok_id)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                int followingCount = jsonObject
                        .getAsJsonObject("data")
                        .getAsJsonObject("stats")
                        .get("followingCount")
                        .getAsInt();
                return followingCount;
            }else if (jsonObject.get("msg").getAsString().contains("unique_id is invalid")) {
                // Lấy followingCount từ data.stats
                return -1;
            }
        } catch (Exception e) {
            return -2;
        }
        return -2;
    }

    public static JsonObject getInfoChannelBy(String tiktok_id) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/info?unique_id="+tiktok_id)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                JsonObject infoChannel = jsonObject
                        .getAsJsonObject("data")
                        .getAsJsonObject("stats");

                return infoChannel;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static JsonObject getInfoFullChannelByUserId(String user_id) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/info?user_id="+user_id)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                JsonObject infoChannel = jsonObject
                        .getAsJsonObject("data");

                return infoChannel;
            }else if (jsonObject.get("msg").getAsString().contains("unique_id is invalid")) {
                // Lấy followerCount từ data.stats
                return new JsonObject();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static JsonObject getInfoFullChannel(String tiktok_id) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/info?unique_id="+tiktok_id)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                JsonObject infoChannel = jsonObject
                        .getAsJsonObject("data");

                return infoChannel;
            }else if (jsonObject.get("msg").getAsString().contains("unique_id is invalid")) {
                // Lấy followerCount từ data.stats
                return new JsonObject();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }


    public static String getId(String tiktok_id) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/info?unique_id="+tiktok_id)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                String id = jsonObject
                        .getAsJsonObject("data")
                        .getAsJsonObject("user")
                        .get("id")
                        .getAsString();
                return id;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    public static Integer getFollowerCount2(String tiktok_id,Integer index) {
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("trong1-1.byeip.net", 16000));
            Document doc = Jsoup.connect("https://livecounts.io/tiktok-live-follower-counter/"+tiktok_id).proxy(proxy).get();
            TimeUnit.SECONDS.sleep(2);
            Elements scriptElements = doc.select("script");
            for (Element scriptElement : scriptElements) {
                String scriptContent = scriptElement.html();
                if (scriptContent.contains("pageProps")) {
                    int startIndex = scriptContent.indexOf("{");
                    int endIndex = scriptContent.lastIndexOf("}") + 1;
                    String jsonString = scriptContent.substring(startIndex, endIndex);
                    //System.out.println(jsonString);
                    JsonReader reader = new JsonReader(new StringReader(jsonString));
                    reader.setLenient(true);
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    JsonObject jsonObject =  jsonElement.getAsJsonObject();
                    if (jsonObject.has("props")) {
                        JsonObject props = jsonObject.getAsJsonObject("props");

                        // Kiểm tra "pageProps" tồn tại trong props
                        if (props.has("pageProps")) {
                            JsonObject pageProps = props.getAsJsonObject("pageProps");

                            // Lấy dữ liệu từ pageProps
                            if (pageProps.has("data")) {
                                JsonObject data = pageProps.getAsJsonObject("data");
                                if(data.get("success").getAsBoolean()){
                                    JsonObject stats = data.getAsJsonObject("stats");
                                    //System.out.println(stats.get("followers").getAsInt());
                                    return stats.get("followers").getAsInt();
                                }

                            } else {
                               return -2;
                            }
                        } else {
                            return -2;
                        }
                    } else {
                        return -2;
                    }
                }
            }
            return -2;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return -2;
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
            String link="https://www.tiktok.com/@/video/"+video_id;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/?url="+link)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                int liveCount = jsonObject
                        .getAsJsonObject("data")
                        .get("digg_count")
                        .getAsInt();
                return liveCount;
            }else if (jsonObject.get("msg").getAsString().contains("Url parsing is failed")) {
                // Lấy digg_count từ data.stats
                return -1;
            }
        } catch (Exception e) {
            return -2;
        }
        return -2;
    }

    public static Integer getCountFavorites(String video_id) {

        try {
            String link="https://www.tiktok.com/@/video/"+video_id;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/?url="+link)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                int liveCount = jsonObject
                        .getAsJsonObject("data")
                        .get("collect_count")
                        .getAsInt();
                return liveCount;
            }else if (jsonObject.get("msg").getAsString().contains("Url parsing is failed")) {
                // Lấy digg_count từ data.stats
                return -1;
            }
        } catch (Exception e) {
            return -2;
        }
        return -2;
    }

    public static Integer getCountShare(String video_id) {

        try {
            String link="https://www.tiktok.com/@/video/"+video_id;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/?url="+link)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                int liveCount = jsonObject
                        .getAsJsonObject("data")
                        .get("share_count")
                        .getAsInt();
                return liveCount;
            }else if (jsonObject.get("msg").getAsString().contains("Url parsing is failed")) {
                // Lấy digg_count từ data.stats
                return -1;
            }
        } catch (Exception e) {
            return -2;
        }
        return -2;
    }

    public static JsonObject getInfoVideo(String link) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/?url="+link)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                JsonObject jsonData = jsonObject
                        .getAsJsonObject("data");
                return jsonData;
            }else{
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonArray getInfoVideoByChannel(String tiktok_id,Integer count) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/posts?unique_id="+tiktok_id+"&count="+count)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                JsonArray jsonData = jsonObject
                        .getAsJsonObject("data").getAsJsonArray("videos");
                if(jsonData.size()==0){
                    return null;
                }else{
                    return jsonData;
                }
            }else{
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonArray getInfoVideoByChannelByUserId(String user_id,Integer count) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/posts?user_id="+user_id+"&count="+count)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                JsonArray jsonData = jsonObject
                        .getAsJsonObject("data").getAsJsonArray("videos");
                if(jsonData.size()==0){
                    return null;
                }else{
                    return jsonData;
                }
            }else{
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }


    public static JsonElement getUserByKeyword(String keyword, Integer count, AccountCloneRepository accountCloneRepository) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/user/search?keywords="+keyword+"&count="+count)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                JsonArray jsonData = jsonObject
                        .getAsJsonObject("data").getAsJsonArray("user_list");
                if(jsonData.size()==0){
                    return null;
                }else{
                    for (JsonElement user :jsonData
                         ) {
                        if(user.getAsJsonObject().getAsJsonObject("stats").get("videoCount").getAsInt()>5&&
                                user.getAsJsonObject().getAsJsonObject("stats").get("followerCount").getAsInt()<5000&&
                                user.getAsJsonObject().getAsJsonObject("user").get("verified").getAsBoolean()==false&&
                                user.getAsJsonObject().getAsJsonObject("user").get("privateAccount").getAsBoolean()==false&&
                                accountCloneRepository.check_Id_Clone_By_Id_Clone(user.getAsJsonObject().getAsJsonObject("user").get("id").getAsString())==0
                        ){
                            System.out.println(user);
                            return user;
                        }

                    }
                    return null;
                }
            }else{
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getCountView(String video_id) {

        try {
            String link="https://www.tiktok.com/@/video/"+video_id;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/?url="+link)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                int liveCount = jsonObject
                        .getAsJsonObject("data")
                        .get("play_count")
                        .getAsInt();
                return liveCount;
            }else if (jsonObject.get("msg").getAsString().contains("Url parsing is failed")) {
                // Lấy play_count từ data.stats
                return -1;
            }
        } catch (Exception e) {
            return -2;
        }
        return -2;
    }
    public static Integer getCountComment(String video_id) {

        try {
            String link="https://www.tiktok.com/@/video/"+video_id;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url("https://tiktok-video-feature-summary.p.rapidapi.com/?url="+link)
                    .addHeader("x-rapidapi-host", "tiktok-video-feature-summary.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", getKey())
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if ("success".equals(jsonObject.get("msg").getAsString())) {
                // Lấy followerCount từ data.stats
                int liveCount = jsonObject
                        .getAsJsonObject("data")
                        .get("comment_count")
                        .getAsInt();
                return liveCount;
            }else if (jsonObject.get("msg").getAsString().contains("Url parsing is failed")) {
                // Lấy comment_count từ data.stats
                return -1;
            }
        } catch (Exception e) {
            return -2;
        }
        return -2;
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

    public static String extractTikTokId(String input) {
        // Xóa phần query string nếu có (ví dụ: ?lang=vi-VN)
        String cleanInput = input.split("\\?")[0];

        // Regex để lấy username TikTok (có thể có hoặc không có "@")
        Pattern pattern = Pattern.compile("^@?([a-zA-Z0-9_.]+)$");
        Matcher matcher = pattern.matcher(cleanInput);

        if (matcher.find()) {
            return "@" + matcher.group(1);
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
        String pattern = "tiktok\\.com/.*/(video|photo)/(\\d+)";
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
