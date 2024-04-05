package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name="proxy_ipv4_tiktok")
public class Proxy_IPV4_TikTok {
    @Id
    private String proxy;
    private Long time_get;
    private String ipv4;
    private Integer running;
    private String type_proxy;

    public Proxy_IPV4_TikTok() {
    }

    public Proxy_IPV4_TikTok(String proxy, Long time_get, String ipv4, Integer running, String type_proxy) {
        this.proxy = proxy;
        this.time_get = time_get;
        this.ipv4 = ipv4;
        this.running = running;
        this.type_proxy = type_proxy;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Long getTime_get() {
        return time_get;
    }

    public void setTime_get(Long time_get) {
        this.time_get = time_get;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public String getType_proxy() {
        return type_proxy;
    }

    public void setType_proxy(String type_proxy) {
        this.type_proxy = type_proxy;
    }

}
