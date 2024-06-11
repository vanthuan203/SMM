package com.nts.awspremium;

import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.net.Authenticator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TikTokApi {
    public static Integer getFollowerCountOFF(String tiktok_link,String proxycheck) {
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        //System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
        String[] proxycut = proxycheck.split(":");

        try {
            //System.out.println(proxycut[0]+":"+proxycut[1]+":"+proxycut[2]+":"+ proxycut[3]);
            URL url = new URL(tiktok_link.trim());
            java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxycut[0], Integer.parseInt(proxycut[1])));
            if (proxycut.length > 2) {

                java.net.Authenticator authenticator = new java.net.Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication(proxycut[2],
                                proxycut[3].toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            conn.connect();
            int code = conn.getResponseCode();
            if(code==200){
                try{
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    conn.disconnect();
                    String follower=response.substring(response.indexOf("followerCount"));
                    follower=follower.substring(follower.indexOf(":")+1).split(",")[0];
                    return Integer.parseInt(follower);
                }
                catch (Exception e){
                    return -1;
                }

            }else{
                conn.disconnect();
                return -2;
            }
        } catch (Exception e) {
            return -3;
        }
    }
    public static Integer getFollowerCount(String tiktok_id) {

        try {

            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("https://countik.com/api/exist/" + tiktok_id).get().build();

            Response response = client.newCall(request).execute();

            String resultJson = response.body().string();
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            if(jsonObject.get("sec_uid")==null){
                return -2;
            }else{
                request = new Request.Builder().url("https://countik.com/api/userinfo?sec_user_id=" + jsonObject.get("sec_uid").toString()).get().build();

                response = client.newCall(request).execute();

                resultJson = response.body().string();
                obj = new JSONParser().parse(resultJson);
                jsonObject = (JSONObject) obj;
                if(jsonObject.get("followerCount")==null){
                    return -2;
                }
                System.out.println(jsonObject.get("followerCount"));
                return Integer.parseInt(jsonObject.get("followerCount").toString());
            }

        } catch (Exception e) {
            return -2;
        }
    }

    public static JSONObject getInfoVideoTikTok(String video_id) {

        try {
            JSONObject json = new JSONObject();
            String link="https://www.tiktok.com/video/"+video_id;
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
            Object obj = new JSONParser().parse(resultJson);
            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject;
        } catch (Exception e) {
            return null;
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

}
