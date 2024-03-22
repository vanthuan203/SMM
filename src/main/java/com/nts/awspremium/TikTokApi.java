package com.nts.awspremium;

import com.nts.awspremium.repositories.ProxyRepository;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.nts.awspremium.repositories.ProxyRepository;
public class TikTokApi {
    public static Integer getFollowerCount(String tiktok_link,String proxycheck) {
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

}
