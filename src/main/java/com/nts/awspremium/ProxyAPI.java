package com.nts.awspremium;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.*;

public class ProxyAPI {
    public static boolean checkProxy(String proxycheck) {
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        //System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
        String[] proxycut = proxycheck.split(":");

        try {
            //System.out.println(proxycut[0]+":"+proxycut[1]+":"+proxycut[2]+":"+ proxycut[3]);
            URL url = new URL("https://www.google.com/");
            java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxycut[0], Integer.parseInt(proxycut[1])));
            if (proxycut.length > 2) {

                Authenticator authenticator = new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication(proxycut[2],
                                proxycut[3].toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            conn.connect();
            int code = conn.getResponseCode();
            //System.out.println("Status:"+proxycut+" - "+code);
            //String contents = conn.getResponseMessage();
            //System.out.println("Status:"+contents);
            conn.disconnect();
            if (code == 200 || code == 429 || code ==404) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //System.out.println(e);
            if(e.toString().indexOf("Authentication")>=0){
                return true;
            }
            return false;
        }
    }


    public static String getSock5(String geo) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://info.proxy.ipmars.com/extractProxyIp?regions="+geo+"&num=1&protocol=socks5&return_type=json&lh=1&st=%5C")
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if (jsonObject.get("success").getAsBoolean()) {
                JsonObject data = jsonObject.getAsJsonArray("data").get(0).getAsJsonObject();
                return data.get("ip").getAsString()+":"+data.get("port").getAsInt();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

}
