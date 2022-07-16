package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "historysum")
@Entity
public class HistorySum {
    @Id
    private Long id;
    private String username;
    private String vps;
    private String channelid;
    private String videoid;
    private String proxy;
    public HistorySum() {
    }

    public HistorySum(Long id, String username, String vps, String channelid, String videoid,String proxy ) {
        this.id = id;
        this.username = username;
        this.vps = vps;
        this.channelid = channelid;
        this.videoid = videoid;
        this.proxy = proxy;
    }

    @Override
    public String toString() {
        return "HistorySum{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", vps='" + vps + '\'' +
                ", channelid='" + channelid + '\'' +
                ", videoid='" + videoid + '\'' +
                ", proxy='" + proxy + '\'' +
                '}';
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
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

    public String getVps() {
        return vps;
    }

    public void setVps(String vps) {
        this.vps = vps;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public String getVideoid() {
        return videoid;
    }

    public void setVideoid(String videoid) {
        this.videoid = videoid;
    }
}
