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

public class GoogleApi {
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
