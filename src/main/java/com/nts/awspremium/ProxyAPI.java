package com.nts.awspremium;

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
    public static boolean checkResponseCode (String link) {
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        //System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");

        try {
            URL url = new URL(link.trim());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);

            conn.connect();
            //System.out.println(proxycut[0]+":"+proxycut[1]+":"+proxycut[2]+":"+ proxycut[3]);
            int code = conn.getResponseCode();
            //String contents = conn.getResponseMessage();
            conn.disconnect();
            if (code == 200 || code == 429) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
