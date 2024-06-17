package com.nts.awspremium;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.nts.awspremium.controller.GoogleKeyController;
import com.nts.awspremium.repositories.GoogleKeyRepository;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class GoogleApi {
    public static String getYoutubeId(String url) {
        String pattern = "https?:\\/\\/(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*";

        Pattern compiledPattern = Pattern.compile(pattern,
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }/*from w  w  w.  j a  va  2 s .c om*/
        return null;
    }

    public static Integer getCountLike(String order_key,String key){
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Random ran = new Random();
            Request request = null;
            Iterator k = null;
            request = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key="+key.trim()+"&fields=items(statistics(likeCount))&part=statistics&id=" + order_key).get().build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson1 = response.body().string();
                Object obj1 = new JSONParser().parse(resultJson1);
                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    return -2;
                }
                k = items.iterator();
                if (k.hasNext() == false) {
                    return -1;
                }
                JSONObject video = (JSONObject) k.next();
                JSONObject statistics = (JSONObject) video.get("statistics");
                return Integer.parseInt(statistics.get("likeCount").toString());
            }else{
                return -2;
            }
        } catch (IOException | ParseException e) {
            return -2;
        }

    }

    public static Integer getCountComment(String order_key,String key){
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Random ran = new Random();
            Request request = null;
            Iterator k = null;
            request = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key="+key.trim()+"&fields=items(statistics(commentCount))&part=statistics&id=" + order_key).get().build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson1 = response.body().string();
                Object obj1 = new JSONParser().parse(resultJson1);
                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    return -2;
                }
                k = items.iterator();
                if (k.hasNext() == false) {
                    return -1;
                }
                JSONObject video = (JSONObject) k.next();
                JSONObject statistics = (JSONObject) video.get("statistics");
                return Integer.parseInt(statistics.get("commentCount").toString());
            }else{
                return -2;
            }
        } catch (IOException | ParseException e) {
            return -2;
        }

    }

    public static Integer getCountView(String order_key,String key){
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Random ran = new Random();
            Request request = null;
            Iterator k = null;
            request = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key="+key.trim()+"&fields=items(statistics(viewCount))&part=statistics&id=" + order_key).get().build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson1 = response.body().string();
                Object obj1 = new JSONParser().parse(resultJson1);
                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    return -2;
                }
                k = items.iterator();
                if (k.hasNext() == false) {
                    return -1;
                }
                JSONObject video = (JSONObject) k.next();
                JSONObject statistics = (JSONObject) video.get("statistics");
                return Integer.parseInt(statistics.get("viewCount").toString());
            }else{
                return -2;
            }
        } catch (IOException | ParseException e) {
            return -2;
        }

    }
    public static Integer getCountSubcriber(String order_key){
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("https://api.socialcounts.org/youtube-live-subscriber-count/" + order_key).get().build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson1 = response.body().string();
                Object obj1 = new JSONParser().parse(resultJson1);
                JSONObject jsonObject1 = (JSONObject) obj1;
                return Integer.parseInt(jsonObject1.get("est_sub").toString());
            }else{
                return -2;
            }
        } catch (IOException | ParseException e) {
            return -2;
        }

    }
    public static Integer getCountLikeCurrent(String order_key){
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("https://api.socialcounts.org/youtube-video-live-view-count/" + order_key).get().build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson = response.body().string();
                Object obj = new JsonParser().parse(resultJson);
                JsonObject jsonObject = (JsonObject) obj;

                // Get the table array
                JsonArray tableArray = jsonObject.getAsJsonArray("table");

                // Iterate through the table array to find the Like Count
                for (JsonElement element : tableArray) {
                    JsonObject tableItem = element.getAsJsonObject();
                    if (tableItem.get("name").getAsString().equals("Like Count")) {
                        return  tableItem.get("count").getAsInt();
                    }
                }
                return -2;
            }else{
                return -2;
            }
        } catch (Exception e) {
            return -2;
        }
    }
    public static String getChannelId(String channelUrl) {
        try {
            // Kết nối tới trang YouTube và lấy nội dung trang
            Document doc = Jsoup.connect(channelUrl).get();

            // Tìm tất cả các thẻ <script> chứa đoạn JSON
            Elements scriptElements = doc.select("script");
            for (Element scriptElement : scriptElements) {
                String scriptContent = scriptElement.html();
                if (scriptContent.contains("channelId")) {
                    // Lấy phần JSON trong nội dung của thẻ script
                    int startIndex = scriptContent.indexOf("{");
                    int endIndex = scriptContent.lastIndexOf("}") + 1;
                    String jsonString = scriptContent.substring(startIndex, endIndex);
                    // Phân tích cú pháp JSON và trích xuất videoId
                    JsonReader reader = new JsonReader(new StringReader(jsonString));
                    reader.setLenient(true);
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    JsonObject jsonObject =  jsonElement.getAsJsonObject();
                    //System.out.println(jsonObject);
                    String id = jsonObject.getAsJsonObject("header")
                            .getAsJsonObject("c4TabbedHeaderRenderer")
                            .get("channelId").toString().replace("\"","");
                    return id;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    public static List<String> getVideoLinks(String channelUrl) {
        List<String> videoList = new ArrayList<>();

        try {
            // Kết nối tới trang YouTube và lấy nội dung trang
            Document doc = Jsoup.connect(channelUrl).get();

            // Tìm tất cả các thẻ <script> chứa đoạn JSON
            Elements scriptElements = doc.select("script");
            for (Element scriptElement : scriptElements) {
                String scriptContent = scriptElement.html();
                if (scriptContent.contains("videoRenderer")) {
                    // Lấy phần JSON trong nội dung của thẻ script
                    int startIndex = scriptContent.indexOf("{");
                    int endIndex = scriptContent.lastIndexOf("}") + 1;
                    String jsonString = scriptContent.substring(startIndex, endIndex);
                    // Phân tích cú pháp JSON và trích xuất videoId
                    JsonReader reader = new JsonReader(new StringReader(jsonString));
                    reader.setLenient(true);
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    JsonObject jsonObject =  jsonElement.getAsJsonObject();
                    //System.out.println(jsonObject);
                    JsonArray items = jsonObject.getAsJsonObject("contents")
                            .getAsJsonObject("twoColumnBrowseResultsRenderer")
                            .getAsJsonArray("tabs");

                    JsonArray item_video=items.get(1).getAsJsonObject().
                            getAsJsonObject("tabRenderer")
                            .getAsJsonObject("content")
                            .getAsJsonObject("richGridRenderer")
                            .getAsJsonArray("contents");
                    if(item_video.size()>0){
                        for (int i=0;i<item_video.size();i++){
                            try{
                                String video_id=item_video.get(i).getAsJsonObject().
                                        getAsJsonObject("richItemRenderer")
                                        .getAsJsonObject("content")
                                        .getAsJsonObject("videoRenderer").get("videoId").toString().replace("\"","");
                                String video_title=item_video.get(i).getAsJsonObject().
                                        getAsJsonObject("richItemRenderer")
                                        .getAsJsonObject("content")
                                        .getAsJsonObject("videoRenderer")
                                        .getAsJsonObject("title")
                                        .getAsJsonArray("runs").get(0).getAsJsonObject().get("text").toString().replace("\"","");
                                String video_duration=item_video.get(i).getAsJsonObject().
                                        getAsJsonObject("richItemRenderer")
                                        .getAsJsonObject("content")
                                        .getAsJsonObject("videoRenderer")
                                        .getAsJsonObject("lengthText").get("simpleText").toString().replace("\"","");
                                String[] duration_Text=video_duration.toString().split(":");
                                Long duration=0L;
                                if(duration_Text.length==4){
                                    duration=Long.parseLong(duration_Text[0].trim())*3600*24+Long.parseLong(duration_Text[0].trim())*3600+Long.parseLong(duration_Text[1].trim())*60+Long.parseLong(duration_Text[2].trim());
                                }else if(duration_Text.length==3){
                                    duration=Long.parseLong(duration_Text[0].trim())*3600+Long.parseLong(duration_Text[1].trim())*60+Long.parseLong(duration_Text[2].trim());
                                }else if(duration_Text.length==2){
                                    duration=Long.parseLong(duration_Text[0].trim())*60+Long.parseLong(duration_Text[1].trim());
                                    if(duration<90){
                                        continue;
                                    }
                                }else{
                                    continue;
                                }
                                videoList.add(video_id+"~#"+video_title+"~#"+duration) ;
                                if(videoList.size()>=5){
                                    break;
                                }
                            }catch (Exception e){
                                System.out.println(e.getMessage());
                            }

                        }
                    }
                }
            }
            return videoList;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return videoList;
    }
}
