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
            conn.setConnectTimeout(1000);

            conn.connect();
            //System.out.println(proxycut[0]+":"+proxycut[1]+":"+proxycut[2]+":"+ proxycut[3]);
            int code = conn.getResponseCode();
            //System.out.println("Status:"+code);
            //String contents = conn.getResponseMessage();
            conn.disconnect();
            if (code == 200 || code == 429 || code ==404) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
