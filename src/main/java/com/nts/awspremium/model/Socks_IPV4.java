package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "socks_ipv4")
public class Socks_IPV4 {
    @Id
    private String ip ;
    private String ipv4;
    private String ipv4_old;
    private String auth;
    private Long timeupdate;
    public Socks_IPV4() {
    }

    public Socks_IPV4(String ip, String ipv4, String ipv4_old, String auth, Long timeupdate) {
        this.ip = ip;
        this.ipv4 = ipv4;
        this.ipv4_old = ipv4_old;
        this.auth = auth;
        this.timeupdate = timeupdate;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public String getIpv4_old() {
        return ipv4_old;
    }

    public void setIpv4_old(String ipv4_old) {
        this.ipv4_old = ipv4_old;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public Long getTimeupdate() {
        return timeupdate;
    }

    public void setTimeupdate(Long timeupdate) {
        this.timeupdate = timeupdate;
    }
}
