package com.nts.awspremium;

import com.fasterxml.jackson.databind.util.JSONPObject;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    public static String getVideoBuChannelId(String channelid){

        ArrayList videosID = new ArrayList<String>();
        try {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "");
                Request request = new Request.Builder()
                        .url("https://backend.simplesolution.co/UserDataApi/channelVideosInfoNonKey?list_video=S7ElVoYZN0g"+channelid)
                        .method("GET", body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                String resultJson=response.body().string();
                Object obj = new JSONParser().parse(resultJson);
                JSONObject jsonpObject=(JSONObject) obj;
                JSONArray items=(JSONArray) jsonpObject.get("videos");
                Iterator i = items.iterator();
                while (i.hasNext()){
                    try{
                        JSONObject video = (JSONObject) i.next();
                       videosID.add(video.get("videoId").toString());
                    }catch (Exception e){

                    }
                }
                return String.join("[videosplit]",videosID);
            } catch (IOException | ParseException e) {

            }
        return "";
    }
}
