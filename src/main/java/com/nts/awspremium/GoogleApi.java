package com.nts.awspremium;

import com.fasterxml.jackson.databind.util.JSONPObject;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    public static List<String> getVideoLinks(String channelUrl) throws IOException {
        List<String> videoLinks = new ArrayList<>();
        Document doc = Jsoup.parse(channelUrl);
        // Tìm tất cả các thẻ <a> có thuộc tính href
        // Mẫu regex để khớp với các liên kết video
        Pattern pattern = Pattern.compile("^/watch\\?v=.*");
        Elements links = doc.getElementsMatchingText(pattern);
        System.out.println(links);

        for (Element link : links) {
            String href = link.attr("href");
            Matcher matcher = pattern.matcher(href);
            if (matcher.find()) {
                videoLinks.add("https://www.youtube.com" + href);
            }
        }

        return videoLinks;
    }
}
