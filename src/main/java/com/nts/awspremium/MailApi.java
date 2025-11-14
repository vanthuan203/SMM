package com.nts.awspremium;

import com.google.gson.JsonObject;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MailApi {


      public static Boolean createMail(String mail){
            try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("http://mail.oliverstonecollege.com:8888/email/create-one?email=" + mail).get().build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson = response.body().string();
                response.body().close();
                Object obj = new JSONParser().parse(resultJson);
                JSONObject jsonObject = (JSONObject) obj;
                return Boolean.parseBoolean(jsonObject.get("success").toString());
            }else{
                return false;
            }
        } catch (IOException | ParseException e) {
            return false;
        }

    }

    public static String ipCheck(String ip){
          try{
              try {
                  OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                  Request request = null;
                  request = new Request.Builder().url("http://ip-api.com/json/" + ip).get().build();
                  Response response = client.newCall(request).execute();
                  if(response.isSuccessful()){
                      String resultJson = response.body().string();
                      response.body().close();
                      Object obj = new JSONParser().parse(resultJson);
                      JSONObject jsonObject = (JSONObject) obj;
                      if(jsonObject.get("status").toString().equals("success")){
                          return jsonObject.get("as").toString();
                      }else{
                          return null;
                      }
                  }else{
                      return null;
                  }
              } catch (IOException | ParseException e) {
                  return null;
              }
          }catch (Exception e){
              return  null;
          }


    }

    public static JSONArray getDomains(){
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("http://mail.oliverstonecollege.com:8888/email/domains").get().build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson = response.body().string();
                response.body().close();
                Object obj = new JSONParser().parse(resultJson);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray data = (JSONArray) jsonObject.get("data");
                return data;
            }else{
                return null;
            }
        } catch (IOException | ParseException e) {
            return null;
        }

    }

    public static String getTokenMailTM(String mail, String pass){
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("address", mail);
            jsonBody.put("password", pass);
            RequestBody body = RequestBody.create(mediaType, jsonBody.toString());
            Request request = new Request.Builder()
                    .url("https://api.mail.tm/token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson = response.body().string();
                response.body().close();
                Object obj = new JSONParser().parse(resultJson);
                JSONObject jsonObject = (JSONObject) obj;
                return jsonObject.get("token").toString();
            }else{
                return null;
            }
        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public static String getMessages(String token){
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.mail.tm/messages")
                    .addHeader("Authorization", "Bearer "+token.trim()).get()
                    .build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson = response.body().string();
                response.body().close();
                Object obj = new JSONParser().parse(resultJson);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray messages = (JSONArray) jsonObject.get("hydra:member");

                String targetAddress = "noreply@account.tiktok.com";
                long now = System.currentTimeMillis();
                long limitMillis = now - 48 * 60 * 1000; // 15 phút
                for (int i = 0; i < messages.size(); i++) {
                    JSONObject msg = (JSONObject) messages.get(i);
                    String createdAtStr = msg.get("createdAt").toString();  // ví dụ: 2025-11-14T12:57:45+00:00

                    // Parse ISO 8601
                    Instant createdInstant = Instant.parse(createdAtStr.replace("+00:00", "Z"));
                    long createdMillis = createdInstant.toEpochMilli();

                    // Check điều kiện trong 15 phút
                    if (createdMillis < limitMillis) {
                        continue;
                    }
                    JSONObject from = (JSONObject) msg.get("from");
                    String fromAddress = from.get("address").toString();

                    if (fromAddress.equalsIgnoreCase(targetAddress)) {
                        return msg.get("id").toString();
                    }
                }
            }else{
                return null;
            }
        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String getCode(String mail, String pass){
        try {
            String token =getTokenMailTM(mail,pass);
            if(token!=null){
                String id=getMessages(token);
                if(id!=null){
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    Request request = new Request.Builder()
                            .url("https://api.mail.tm/messages/"+id.trim())
                            .addHeader("Authorization", "Bearer "+token.trim())
                            .get()
                            .build();
                    Response response = client.newCall(request).execute();
                    if(response.isSuccessful()){
                        String resultJson = response.body().string();
                        response.body().close();
                        Object obj = new JSONParser().parse(resultJson);
                        JSONObject jsonObject = (JSONObject) obj;
                        Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
                        Matcher matcher = pattern.matcher(jsonObject.get("intro").toString());
                        if (matcher.find()) {
                            return matcher.group();  // trả về mã ví dụ "851752"
                        }
                        return "Không lấy đc code! Thử lại";
                    }else{
                        return null;
                    }
                }else{
                    return "Không có message trong 48h gần nhất! Thử lại";
                }
            }else{
                return "Sai email/password";
            }

        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


}
