package com.nts.awspremium.model;

import javax.persistence.*;

@Table(name = "historyview")
@Entity
public class HistoryView {
    @Id
    private Long id;
    private String username;
    private String listvideo;
    private String proxy;
    private String vps;
    private Integer running;
    private String channelid;
    private Long timeget;
    private String typeproxy;
    private String geo;
    private String videoid;

    public HistoryView() {
    }

    public HistoryView(Long id, String username, String listvideo, String proxy, String vps, Integer running, String channelid, Long timeget, String typeproxy, String geo, String videoid) {
        this.id = id;
        this.username = username;
        this.listvideo = listvideo;
        this.proxy = proxy;
        this.vps = vps;
        this.running = running;
        this.channelid = channelid;
        this.timeget = timeget;
        this.typeproxy = typeproxy;
        this.geo = geo;
        this.videoid = videoid;
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

    public Long getTimeget() {
        return timeget;
    }

    public void setTimeget(Long timeget) {
        this.timeget = timeget;
    }

    public String getTypeproxy() {
        return typeproxy;
    }

    public void setTypeproxy(String typeproxy) {
        this.typeproxy = typeproxy;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public String getVideoid() {
        return videoid;
    }

    public void setVideoid(String videoid) {
        this.videoid = videoid;
    }
}
