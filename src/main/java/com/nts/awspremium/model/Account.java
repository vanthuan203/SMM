package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String recover;
    private Integer live;
    private String encodefinger;
    private String cookie;
    private Long endtrial;
    private String endtrialstring;
    private String vps;
    private String proxy;
    private Integer running;
    public Account() {
    }

    public Account(String username, String password, String recover, Integer live, String encodefinger, String cookie, Long endtrial, String endtrialstring, String vps, String proxy, Integer running) {
        this.username = username;
        this.password = password;
        this.recover = recover;
        this.live = live;
        this.encodefinger = encodefinger;
        this.cookie = cookie;
        this.endtrial = endtrial;
        this.endtrialstring = endtrialstring;
        this.vps = vps;
        this.proxy = proxy;
        this.running = running;
    }

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", recover='" + recover + '\'' +
                ", live=" + live +
                ", endtrialstring='" + endtrialstring + '\'' +
                ", vps='" + vps + '\'' +
                ", proxy='" + proxy + '\'' +
                ", running=" + running +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRecover() {
        return recover;
    }

    public void setRecover(String recover) {
        this.recover = recover;
    }

    public Integer getLive() {
        return live;
    }

    public void setLive(Integer live) {
        this.live = live;
    }

    public String getEncodefinger() {
        return encodefinger;
    }

    public void setEncodefinger(String encodefinger) {
        this.encodefinger = encodefinger;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public Long getEndtrial() {
        return endtrial;
    }

    public void setEndtrial(Long endtrial) {
        this.endtrial = endtrial;
    }

    public String getEndtrialstring() {
        return endtrialstring;
    }

    public void setEndtrialstring(String endtrialstring) {
        this.endtrialstring = endtrialstring;
    }

    public String getVps() {
        return vps;
    }

    public void setVps(String vps) {
        this.vps = vps;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

}

