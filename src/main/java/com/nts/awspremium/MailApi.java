package com.nts.awspremium;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


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

}
