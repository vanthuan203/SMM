package com.nts.awspremium;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nts.awspremium.model.MicrosoftMail;
import com.nts.awspremium.repositories.MicrosoftMailRepository;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

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
                response.body().close();
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
                      response.body().close();
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
                response.body().close();
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
                response.body().close();
                return null;
            }
        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }


    public static String checkLiveGmail(String mail){
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("api_key", "29d01e61bd90b602b687ede8eb32c4fd");
            jsonBody.put("fastCheck", false);

            JSONArray emails = new JSONArray();
            emails.add(mail.trim());
            jsonBody.put("emails", emails);
            RequestBody body = RequestBody.create(mediaType, jsonBody.toString());
            Request request = new Request.Builder()
                    .url("https://checkmail.live/check/")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson = response.body().string();
                response.body().close();
                JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();
                JsonArray data = jsonObject.getAsJsonArray("data");
                if (data != null && data.size() > 0) {
                    JsonObject firstChoice = data.get(0).getAsJsonObject();
                    return firstChoice.get("status").getAsString().toLowerCase();
                }else {
                    return  null;
                }
            }else{
                response.body().close();
                return null;
            }
        } catch (IOException e) {
            return null;
        }

    }

    public static Boolean checkStatusGmail(String mail){
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("api_key", "29d01e61bd90b602b687ede8eb32c4fd");
            jsonBody.put("fastCheck", true);

            JSONArray emails = new JSONArray();
            emails.add(mail.trim());
            jsonBody.put("emails", emails);
            RequestBody body = RequestBody.create(mediaType, jsonBody.toString());
            Request request = new Request.Builder()
                    .url("https://checkmail.live/check/")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson = response.body().string();
                response.body().close();
                JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();
                JsonArray data = jsonObject.getAsJsonArray("data");
                if (data != null && data.size() > 0) {
                    JsonObject firstChoice = data.get(0).getAsJsonObject();
                    if(firstChoice.get("status").getAsString().toLowerCase().equals("live")){
                        return true;
                    }else{
                        return false;
                    }
                }else {
                    return  true;
                }
            }else{
                response.body().close();
                return true;
            }
        } catch (IOException e) {
            return true;
        }

    }


    public static String getTokenMailMicrosoft(MicrosoftMail mail){
        try {

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = new FormBody.Builder()
                    .add("client_id", mail.getClient_id().trim())
                    .add("grant_type", "refresh_token")
                    .add("refresh_token", mail.getRefresh_token1().trim())
                    .add("scope", "https://graph.microsoft.com/.default")
                    .build();
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson = response.body().string();
                response.body().close();
                Object obj = new JSONParser().parse(resultJson);
                JSONObject jsonObject = (JSONObject) obj;
                return jsonObject.get("access_token").toString();
            }else{
                response.body().close();
                return null;
            }
        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public static String getMessagesMicrosoft(String token){
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://graph.microsoft.com/v1.0/me/mailFolders/inbox/messages?$top=10&$select=id,from,bodyPreview,receivedDateTime")
                    .addHeader("Authorization", "Bearer "+token.trim()).get()
                    .build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String resultJson = response.body().string();
                response.body().close();
                Object obj = new JSONParser().parse(resultJson);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray messages = (JSONArray) jsonObject.get("value");

                String targetAddress1 = "noreply@account.tiktok.com";
                String targetAddress2 = "register@account.tiktok.com";
                long now = System.currentTimeMillis();
                long limitMillis = now - 48 * 60 * 60 * 1000; // 15 phút
                for (int i = 0; i < messages.size(); i++) {
                    JSONObject msg = (JSONObject) messages.get(i);
                    String createdAtStr = msg.get("receivedDateTime").toString();  // ví dụ: 2025-11-14T12:57:45+00:00

                    // Parse ISO 8601
                    Instant createdInstant = Instant.parse(createdAtStr.replace("+00:00", "Z"));
                    long createdMillis = createdInstant.toEpochMilli();

                    // Check điều kiện trong 15 phút
                    if (createdMillis < limitMillis) {
                        continue;
                    }
                    String fromAddress = ((JSONObject)((JSONObject)msg.get("from"))
                            .get("emailAddress"))
                            .get("address")
                            .toString();

                    if (fromAddress.equalsIgnoreCase(targetAddress1) || fromAddress.equalsIgnoreCase(targetAddress2)) {
                        Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
                        Matcher matcher = pattern.matcher(msg.get("bodyPreview").toString());
                        if (matcher.find()) {
                            return msg.get("id").toString();
                        }else{
                            continue;
                        }
                    }
                }
            }else{
                response.body().close();
                return null;
            }
        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return null;
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

                String targetAddress1 = "noreply@account.tiktok.com";
                String targetAddress2 = "register@account.tiktok.com";
                long now = System.currentTimeMillis();
                long limitMillis = now -  48 * 60 * 60 * 1000 ; // 15 phút
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

                    if (fromAddress.equalsIgnoreCase(targetAddress1) || fromAddress.equalsIgnoreCase(targetAddress2)) {

                        Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
                        Matcher matcher = pattern.matcher(msg.get("intro").toString());
                        if (matcher.find()) {
                            return msg.get("id").toString();
                        }else{
                            continue;
                        }
                    }
                }
            }else{
                response.body().close();
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
                        response.body().close();
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


    public static String getCodeMailMicrosoft(MicrosoftMail mail){
        try {
            String token =getTokenMailMicrosoft(mail);
            if(token!=null){
                String id=getMessagesMicrosoft(token);
                if(id!=null){
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    Request request = new Request.Builder()
                            .url("https://graph.microsoft.com/v1.0/me/messages/"+id.trim())
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
                        Matcher matcher = pattern.matcher(jsonObject.get("bodyPreview").toString());
                        if (matcher.find()) {
                            return matcher.group();  // trả về mã ví dụ "851752"
                        }
                        return "Không lấy đc code! Thử lại";
                    }else{
                        response.body().close();
                        return null;
                    }
                }else{
                    return "Không có message trong 48h gần nhất! Thử lại";
                }
            }else{
                return "Sai Authorization";
            }

        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


}
