package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "account_reg_tiktok")
public class AccountRegTiktok {
    @Id
    private String username;
    private String password;
    private String recover;
    private Integer live;
    private Long time_add;
    private String vps;
    private String device_id;
    private String proxy="";
    private String authy;
    private Integer running;
    private Long time_check;
    private String code="";
    public AccountRegTiktok() {
    }

    public String getAuthy() {
        return authy;
    }

    public void setAuthy(String authy) {
        this.authy = authy;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

