package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "account_tiktok")
public class AccountTiktok {
    @Id
    private String username;
    private String nick_name;
    private String password;
    private String recover;
    private Integer live;
    private Long time_add;
    private String vps;
    private String proxy;
    private Integer running;
    private Long time_check;
    private String geo;
    public AccountTiktok() {
    }

    public AccountTiktok(String username, String password, String recover, Integer live, Long time_add, String vps, String proxy, Integer running, Long time_check, String geo) {
        this.username = username;
        this.password = password;
        this.recover = recover;
        this.live = live;
        this.time_add = time_add;
        this.vps = vps;
        this.proxy = proxy;
        this.running = running;
        this.time_check = time_check;
        this.geo = geo;
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

    public Long getTime_add() {
        return time_add;
    }

    public void setTime_add(Long time_add) {
        this.time_add = time_add;
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

    public Long getTime_check() {
        return time_check;
    }

    public void setTime_check(Long time_check) {
        this.time_check = time_check;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }
}

