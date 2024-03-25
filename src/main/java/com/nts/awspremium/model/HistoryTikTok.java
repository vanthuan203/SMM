package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "history_tiktok")
public class HistoryTikTok {
    @Id
    private String username;
    private String vps;
    private String device_id="";
    private Integer option_running;
    private Integer running;
    private Long timeget;
    private Long orderid;

    public HistoryTikTok() {
    }

    public HistoryTikTok(String username, String vps, String device_id, Integer option_running, Integer running, Long timeget, Long orderid) {
        this.username = username;
        this.vps = vps;
        this.device_id = device_id;
        this.option_running = option_running;
        this.running = running;
        this.timeget = timeget;
        this.orderid = orderid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVps() {
        return vps;
    }

    public void setVps(String vps) {
        this.vps = vps;
    }

    public Integer getOption_running() {
        return option_running;
    }

    public void setOption_running(Integer option_running) {
        this.option_running = option_running;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public Long getTimeget() {
        return timeget;
    }

    public void setTimeget(Long timeget) {
        this.timeget = timeget;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }
}