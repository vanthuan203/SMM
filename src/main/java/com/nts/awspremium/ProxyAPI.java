package com.nts.awspremium;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.*;
import java.net.Authenticator;

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

    public static String getHttpV6(String geo) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("http://server1.idnetwork.com.vn/proxy/getRandProxyByGeo?geo="+geo)
                    .addHeader("Authorization", "t6AsJBTL5WZEJFI2vReFTRCQ5biPQT")
                    .get().build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if (!jsonObject.get("proxy").isJsonNull()) {
                String[] proxy= jsonObject.get("proxy").getAsString().split(":");
                return proxy[0]+":"+proxy[1]+":"+proxy[2]+":"+proxy[3];
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }


    public static String getSock5Luna(String geo) {
        return "socks://user-tiktok_B5onO-region-"+geo+":u89JGqCh3Mv6uin@as.lunaproxy.com:12233";
    }


    public static Boolean add_While_List(String ip) {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8");
            RequestBody body = RequestBody.create(mediaType, "ip="+ip+"&mark="+ip);
            Request request = new Request.Builder()
                    .url("https://webapi.ipmars.com/user/save_white_ip?accept=application/json, text/plain, /&accept-language=en,en-US;q=0.9,vi;q=0.8&authorization=Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHByX3RpbWUiOiIxNzUwNDEyODg0IiwidWlkIjoiMTEyOTUifQ.nr-KD7I4BQBu4eQfXWaz2Dv8S0-_sD_IHasFy6ZvesQ&content-type=application/x-www-form-urlencoded;charset=UTF-8&origin=https://www.ipmars.com&priority=u=1, i&referer=https://www.ipmars.com/personal/whitelist&sec-ch-ua=\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"&sec-ch-ua-mobile=?0&sec-ch-ua-platform=\"Linux\"&sec-fetch-dest=empty&sec-fetch-mode=cors&ec-fetch-site=same-site&user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36&x-fingerprint=9e1800ef61ad76787f84116a048f39d9&x-lang=en&x-timezone=Asia/Saigon")
                    .method("POST", body)
                    .addHeader("accept", "application/json, text/plain, /")
                    .addHeader("accept-language", "en,en-US;q=0.9,vi;q=0.8")
                    .addHeader("authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHByX3RpbWUiOiIxNzUwNDEyODg0IiwidWlkIjoiMTEyOTUifQ.nr-KD7I4BQBu4eQfXWaz2Dv8S0-_sD_IHasFy6ZvesQ")
                    .addHeader("content-type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .addHeader("origin", "https://www.ipmars.com")
                    .addHeader("priority", "u=1, i")
                    .addHeader("referer", "https://www.ipmars.com/personal/whitelist")
                    .addHeader("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"Linux\"")
                    .addHeader("sec-fetch-dest", "empty")
                    .addHeader("sec-fetch-mode", "cors")
                    .addHeader("ec-fetch-site", "same-site")
                    .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
                    .addHeader("x-fingerprint", "9e1800ef61ad76787f84116a048f39d9")
                    .addHeader("x-lang", "en")
                    .addHeader("x-timezone", "Asia/Saigon")
                    .build();
            Response response = client.newCall(request).execute();
            String resultJson = response.body().string();
            response.body().close();
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();

            // Kiểm tra nếu msg là "success"
            if (jsonObject.get("msg").getAsString().equals("success") || jsonObject.get("msg").getAsString().contains("added")) {
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


}
