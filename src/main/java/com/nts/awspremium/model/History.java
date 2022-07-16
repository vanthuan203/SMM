package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "history")
public class History {
    @Id
    private Long id;
    private String username;
    private String listvideo;
    private String proxy;
    private String vps;
    private Integer running;
    private String channelid;
    private Long endtrial;
    private Long timeget;
    public History() {
    }

    public History(Long id, String username, String listvideo, String proxy, String vps, Integer running, String channelid, Long endtrial, Long timeget) {
        this.id = id;
        this.username = username;
        this.listvideo = listvideo;
        this.proxy = proxy;
        this.vps = vps;
        this.running = running;
        this.channelid = channelid;
        this.endtrial = endtrial;
        this.timeget = timeget;
    }

    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", listvideo='" + listvideo + '\'' +
                ", proxy='" + proxy + '\'' +
                ", vps='" + vps + '\'' +
                ", running=" + running +
                ", channelid='" + channelid + '\'' +
                ", endtrial=" + endtrial +
                ", timeget=" + timeget +
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

    public String getListvideo() {
        return listvideo;
    }

    public void setListvideo(String listvideo) {
        this.listvideo = listvideo;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getVps() {
        return vps;
    }

    public void setVps(String vps) {
        this.vps = vps;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public Long getEndtrial() {
        return endtrial;
    }

    public void setEndtrial(Long endtrial) {
        this.endtrial = endtrial;
    }

    public Long getTimeget() {
        return timeget;
    }

    public void setTimeget(Long timeget) {
        this.timeget = timeget;
    }
}
