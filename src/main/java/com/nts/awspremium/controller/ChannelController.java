package com.nts.awspremium.controller;
import com.nts.awspremium.model.ResponseObject;
import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(value = "/channel")
public class ChannelController {
    @GetMapping("/getvideo")
    ResponseEntity<ResponseObject> getvideo(@RequestParam(defaultValue = "") String channelid) throws IOException {
          OkHttpClient client = new OkHttpClient().newBuilder()
                  .build();
          MediaType mediaType = MediaType.parse("application/json");
          RequestBody body = RequestBody.create(mediaType, "");
          Request request = new Request.Builder()
                  .url("https://backend.simplesolution.co/UserDataApi/channelVideosNonKey/"+channelid)
                  .method("GET", body)
                  .addHeader("Content-Type", "application/json")
                  .build();

          Response response = client.newCall(request).execute();
          String resultJson=response.body().string();
          return ResponseEntity.status(HttpStatus.OK).body(
                  new ResponseObject("true","Token không hợp lệ!",resultJson)
          );
      }
}
