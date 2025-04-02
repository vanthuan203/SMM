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
            String prompt="Bạn là AI hỗ trợ đặt tên TikTok ngắn gọn dựa vào ID người dùng cung cấp.  \n" +
                    "Tên bạn tạo phải liên quan rõ ràng đến ID ban đầu và được chọn ngẫu nhiên theo 1 trong 16 phong cách dưới đây:\n" +
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
                    "Ngoài phong cách ngẫu nhiên trên, bạn bắt buộc làm theo lần lượt từng bước dưới đây để có sự ngẫu nhiên chính xác:\n" +
                    "\n" +
                    "- Bước 1: Tạo ngẫu nhiên 1 số (1-100):\n" +
                    "    • Nếu số ≤ 3, tên KHÔNG chứa dấu cách.\n" +
                    "    • Nếu số > 3, tên chứa dấu cách bình thường.\n" +
                    "- Bước 2: Tạo ngẫu nhiên 1 số khác (1-100):\n" +
                    "    • Nếu số ≤ 40, thêm 1 emoji nhỏ (icon đơn giản).\n" +
                    "    • Nếu số > 40, tên không thêm emoji.\n" +
                    "- Bước 3: Tạo ngẫu nhiên 1 số khác (1-100):\n" +
                    "    • Nếu số ≤ 50, thêm đúng 1 ký tự đặc biệt (×, ⚡, α, β,...).\n" +
                    "    • Nếu số > 50, tên không ký tự đặc biệt.\n" +
                    "- Bước 4: Tạo ngẫu nhiên 1 số khác (1-100):\n" +
                    "    • Nếu số ≤ 25, áp dụng phông chữ đặc biệt unicode (vd: \uD835\uDCDD\uD835\uDCEA\uD835\uDCF6\uD835\uDCEE, \uD835\uDD79\uD835\uDD86\uD835\uDD92\uD835\uDD8A...). Nếu áp dụng thì toàn bộ tên phải cùng 1 kiểu font đồng nhất.\n" +
                    "    • Nếu số > 25, tên không dùng phông đặc biệt.\n" +
                    "- Bước 5: Tạo ngẫu nhiên 1 số khác (1-100):\n" +
                    "    • Nếu số ≤ 20, tên thêm số (ví dụ: 07, 99, 24,...).\n" +
                    "    • Nếu số > 20, tên không thêm số.\n" +
                    "\n" +
                    "Bạn CHỈ trả về duy nhất tên TikTok tạo mới đã thực hiện đầy đủ các bước ngẫu nhiên trên (chính xác tỷ lệ như hướng dẫn), TUYỆT ĐỐI không thêm bất kỳ nội dung hay mô tả gì khác vào kết quả.\n" +
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
