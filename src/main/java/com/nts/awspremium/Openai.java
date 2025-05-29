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
            String prompt="Bạn là AI hỗ trợ đặt tên TikTok ngắn gọn dựa vào ID người dùng cung cấp.\n" +
                    "\n" +
                    "Tên bạn tạo phải liên quan rõ ràng đến ID ban đầu, ID ban đầu đang có cấu trúc \"họ+ tên đệm + tên\" và được chọn ngẫu nhiên theo 1 trong 16 phong cách sau:\n" +
                    "\n" +
                    "1. Hài hước, dễ nhớ\n" +
                    "2. Cá tính, ngầu\n" +
                    "3. Đáng yêu, dễ thương\n" +
                    "4. Idol Kpop/USUK\n" +
                    "5. Buồn, tâm trạng\n" +
                    "6. Tên tiếng nước ngoài (Anh, Hàn hoặc Nhật)\n" +
                    "7. Ghép trend vui nhộn\n" +
                    "8. Đơn giản, dễ nhớ\n" +
                    "9. Bí ẩn, cuốn hút\n" +
                    "10. Anime/Manga (Nhật Bản)\n" +
                    "11. Gaming, trò chơi điện tử\n" +
                    "12. Dân chơi hài hước, trẻ trâu\n" +
                    "13. Đồ ăn, thức uống dễ thương\n" +
                    "14. Cổ trang, kiếm hiệp\n" +
                    "15. Thiên nhiên, nhẹ nhàng\n" +
                    "16. Nghiêm túc, trưởng thành, lịch sự\n" +
                    "\n" +
                    "Hãy làm lần lượt theo đúng các bước sau đây, sử dụng các phép ngẫu nhiên (1-100) mỗi bước để quyết định:\n" +
                    "\n" +
                    "- Bước 1: Tạo ngẫu nhiên 1 số x (1-100):\n" +
                    "  • Nếu x ≤ 3, tên KHÔNG chứa dấu cách.\n" +
                    "  • Nếu x > 3, tên chứa dấu cách.\n" +
                    "\n" +
                    "- Bước 2: Tạo ngẫu nhiên 1 số z (1-100):\n" +
                    "  • Nếu z ≤ 25, tên chứa ký tự đặc biệt.( không chứa \") \n" +
                    "  • Nếu z > 25, tên không chứa ký tự đặc biệt.\n" +
                    "\n" +
                    "- Bước 3: Tạo ngẫu nhiên 1 số j (1-100):\n" +
                    "  • Nếu j ≤ 35, tên dùng phông chữ đặc biệt unicode\n" +
                    "  • Nếu j > 35, tên dùng phông chữ mặc định\n" +
                    "\n" +
                    "- Bước 4: Tạo ngẫu nhiên 1 số f (1-100):\n" +
                    "  • Nếu f ≤ 20, tên chứa số.(Nhiều nhất là 2 số)\n" +
                    "  • Nếu f > 20, tên không chứa số.\n" +
                    "\n" +
                    "- Bước 5: Tạo ngẫu nhiên 1 số y (1-100):\n" +
                    "  • Nếu y ≤ 15, tên dùng in hoa.\n" +
                    "  • Nếu y > 15, tên không dùng in hoa.\n" +
                    "\n" +
                    "LƯU Ý ĐẶC BIỆT:\n" +
                    "- Ngôn ngữ tên Tiktok là tiếng anh(en) có ý nghĩa.\n" +
                    "- tên Tiktok phải có ít nhất 1 từ khớp với ID TikTok.\n" +
                    "- Bạn thực hiện đúng theo các bước trên để quyết định ĐẶC ĐIỂM tên.\n" +
                    "- TUYỆT ĐỐI KHÔNG viết thêm giải thích hay trình bày số đã ngẫu nhiên ở từng bước.\n" +
                    "- TUYỆT ĐỐI CHỈ trả về đúng duy nhất tên TikTok cuối cùng.(không chứa \") \n" +
                    "\n" +
                    "ID TikTok: \"@"+mail+"\"\n" +
                    "Tên TikTok mới:";
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(300, TimeUnit.SECONDS) // Time to establish the connection
                    .readTimeout(600, TimeUnit.SECONDS)    // Time to read the response
                    .writeTimeout(600, TimeUnit.SECONDS)   // Time to write data to the server
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("model", "gpt-4o");

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

    public static String nameTiktok2(String name,String key) {

        try {
            Random random=new Random();
            String prompt="Bạn là AI hỗ trợ đặt tên TikTok ngắn gọn dựa vào họ tên người dùng cung cấp.\n" +
                    "\n" +
                    "Tên bạn tạo phải liên quan rõ ràng đến họ tên ban đầu và được chọn ngẫu nhiên theo 1 trong 16 phong cách sau:\n" +
                    "\n" +
                    "1. Hài hước, dễ nhớ\n" +
                    "2. Cá tính, ngầu\n" +
                    "3. Đáng yêu, dễ thương\n" +
                    "4. Idol Kpop/USUK\n" +
                    "5. Buồn, tâm trạng\n" +
                    "6. Tên tiếng nước ngoài (Anh, Hàn hoặc Nhật)\n" +
                    "7. Ghép trend vui nhộn\n" +
                    "8. Đơn giản, dễ nhớ\n" +
                    "9. Bí ẩn, cuốn hút\n" +
                    "10. Anime/Manga (Nhật Bản)\n" +
                    "11. Gaming, trò chơi điện tử\n" +
                    "12. Dân chơi hài hước, trẻ trâu\n" +
                    "13. Đồ ăn, thức uống dễ thương\n" +
                    "14. Cổ trang, kiếm hiệp\n" +
                    "15. Thiên nhiên, nhẹ nhàng\n" +
                    "16. Nghiêm túc, trưởng thành, lịch sự\n" +
                    "\n" +
                    "Hãy làm lần lượt theo đúng các bước sau đây, sử dụng các phép ngẫu nhiên (1-100) mỗi bước để quyết định:\n" +
                    "\n" +
                    "- Bước 1: Tạo ngẫu nhiên 1 số x (1-100):\n" +
                    "  • Nếu x ≤ 3, tên KHÔNG chứa dấu cách.\n" +
                    "  • Nếu x > 3, tên chứa dấu cách.\n" +
                    "\n" +
                    "- Bước 2: Tạo ngẫu nhiên 1 số z (1-100):\n" +
                    "  • Nếu z ≤ 25, tên chứa ký tự đặc biệt.( không chứa \") \n" +
                    "  • Nếu z > 25, tên không chứa ký tự đặc biệt.\n" +
                    "\n" +
                    "- Bước 3: Tạo ngẫu nhiên 1 số j (1-100):\n" +
                    "  • Nếu j ≤ 35, tên dùng phông chữ đặc biệt unicode\n" +
                    "  • Nếu j > 35, tên dùng phông chữ mặc định\n" +
                    "\n" +
                    "- Bước 4: Tạo ngẫu nhiên 1 số f (1-100):\n" +
                    "  • Nếu f ≤ 20, tên chứa số.(Nhiều nhất là 2 số)\n" +
                    "  • Nếu f > 20, tên không chứa số.\n" +
                    "\n" +
                    "- Bước 5: Tạo ngẫu nhiên 1 số y (1-100):\n" +
                    "  • Nếu y ≤ 15, tên dùng in hoa.\n" +
                    "  • Nếu y > 15, tên không dùng in hoa.\n" +
                    "\n" +
                    "LƯU Ý ĐẶC BIỆT:\n" +
                    "- Ngôn ngữ tên Tiktok là tiếng anh(en) có ý nghĩa.\n" +
                    "- tên Tiktok phải có ít nhất 1 từ khớp với họ tên.\n" +
                    "- Bạn thực hiện đúng theo các bước trên để quyết định ĐẶC ĐIỂM tên.\n" +
                    "- TUYỆT ĐỐI KHÔNG viết thêm giải thích hay trình bày số đã ngẫu nhiên ở từng bước.\n" +
                    "- TUYỆT ĐỐI CHỈ trả về đúng duy nhất tên TikTok cuối cùng.(không chứa \") \n" +
                    "\n" +
                    "Họ tên: \""+name+"\"\n" +
                    "Tên TikTok mới:";
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(300, TimeUnit.SECONDS) // Time to establish the connection
                    .readTimeout(600, TimeUnit.SECONDS)    // Time to read the response
                    .writeTimeout(600, TimeUnit.SECONDS)   // Time to write data to the server
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("model", "gpt-4o");

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
