package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "accountsub")
public class AccountSub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String oldpassword;
    private String recover;
    private Integer live;
    private String encodefinger;
    private String cookie;
    private Long timeupdateinfo;
    private Long endtrial;
    private String endtrialstring;
    private String vps;
    private String proxy="";
    private String proxy2;
    private Integer running;
    private Long timecheck;
    private  String date;
    private String geo;
    private Integer timebuff;
    public AccountSub() {
    }

    public AccountSub(Long id, String username, String password, String oldpassword, String recover, Integer live, String encodefinger, String cookie, Long timeupdateinfo, Long endtrial, String endtrialstring, String vps, String proxy, String proxy2, Integer running, Long timecheck, String date, String geo, Integer timebuff) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.oldpassword = oldpassword;
        this.recover = recover;
        this.live = live;
        this.encodefinger = encodefinger;
        this.cookie = cookie;
        this.timeupdateinfo = timeupdateinfo;
        this.endtrial = endtrial;
        this.endtrialstring = endtrialstring;
        this.vps = vps;
        this.proxy = proxy;
        this.proxy2 = proxy2;
        this.running = running;
        this.timecheck = timecheck;
        this.date = date;
        this.geo = geo;
        this.timebuff = timebuff;
    }

    public Long getTimeupdateinfo() {
        return timeupdateinfo;
    }

    public void setTimeupdateinfo(Long timeupdateinfo) {
        this.timeupdateinfo = timeupdateinfo;
    }

    public String getOldpassword() {
        return oldpassword;
    }

    public void setOldpassword(String oldpassword) {
        this.oldpassword = oldpassword;
    }

    public Integer getTimebuff() {
        return timebuff;
    }

    public void setTimebuff(Integer timebuff) {
        this.timebuff = timebuff;
    }

    public String getProxy2() {
        return proxy2;
    }

    public void setProxy2(String proxy2) {
        this.proxy2 = proxy2;
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

    public Long getTimecheck() {
        return timecheck;
    }

    public void setTimecheck(Long timecheck) {
        this.timecheck = timecheck;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }
}

