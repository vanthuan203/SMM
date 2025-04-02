package com.nts.awspremium;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Openai {
    public static String nameTiktok(String mail,String key) {

        try {
            Random random=new Random();
            String prompt="Tạo giúp 1 tôi tên TikTok hay từ địa chỉ email "+mail+",tên kiểu giới trẻ";
            String space=", tên có dấu cách";
            if(random.nextInt(100)<3){
                space=", tên không dấu cách";
            }
            String icon=", tên có 1 icon";
            if(random.nextInt(100)<60){
                icon=", tên không chứa icon";
            }
            String uniky=", tên có ký tự đặc biệt";
            if(random.nextInt(100)<50){
                uniky=", tên không có ký tự đặc biệt";
            }
            String font=", tên có phông đặc biệt";
            if(random.nextInt(100)<65){
                font="";
            }
            String number=", tên có số";
            if(random.nextInt(100)<80){
                number=", tên không có số";
            }
            String end="=> Lưu ý tuyệt đối chỉ trả lời duy nhất tên tiktok";
            prompt=prompt+space+icon+uniky+font+number+end;


            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(300, TimeUnit.SECONDS) // Time to establish the connection
                    .readTimeout(600, TimeUnit.SECONDS)    // Time to read the response
                    .writeTimeout(600, TimeUnit.SECONDS)   // Time to write data to the server
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("model", "gpt-4o-mini");

            // Create the messages array
            JsonArray messagesArray = new JsonArray();

            // First message (developer role)
            JsonObject developerMessage = new JsonObject();
            developerMessage.addProperty("role", "developer");
            developerMessage.addProperty("content", "You are a helpful assistant.");
            messagesArray.add(developerMessage);

            // Second message (user role)
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
            messagesArray.add(userMessage);

            // Add the messages array to the main JSON object
            jsonRequest.add("messages", messagesArray);

            RequestBody body = RequestBody.create(mediaType, jsonRequest.toString());
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer "+key)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String resultJson = response.body().string();
                response.body().close();
                Object obj = new JsonParser().parse(resultJson);
                JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();
                JsonArray choicesArray = jsonObject.getAsJsonArray("choices");
                if (choicesArray != null && choicesArray.size() > 0) {
                    // Lấy phần tử đầu tiên trong mảng "choices"
                    JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();

                    // Truy cập đối tượng "message"
                    JsonObject messageObject = firstChoice.getAsJsonObject("message");
                    if (messageObject != null) {
                        // Lấy giá trị của trường "content"
                        String content = messageObject.get("content").getAsString();
                        return content;
                    } else {
                        return null;
                    }
                }
                // Iterate through the table array to find the Like Count
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static String chatGPT1(String path,String key) {

        File audioFile = new File(path); // Đường dẫn file cần dịch

        try {
            OkHttpClient client = new OkHttpClient();

            // Tạo body cho file upload
            RequestBody fileBody = RequestBody.create(MediaType.parse("audio/mpeg"), audioFile);

            // Tạo request body (multipart form-data)
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("model", "whisper-1")  // Chọn mô hình Whisper
                    .addFormDataPart("response_format", "text")  // Chọn mô hình Whisper
                    .addFormDataPart("file", audioFile.getName(), fileBody)
                    .build();

            // Tạo request gửi đến OpenAI
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/audio/translations")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + key)
                    .build();

            // Gửi request và lấy response
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
               return null;
            }

        } catch (IOException e) {
           return null;
        }
    }
}
