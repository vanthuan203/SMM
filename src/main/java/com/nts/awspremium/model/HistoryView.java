package com.nts.awspremium.model;

import javax.persistence.*;

@Table(name = "historyview")
@Entity
public class HistoryView {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private String videoid;
    private String channelid;
    private Integer duration;
    private Long time;

    public HistoryView() {
    }

    public HistoryView(Long id, String username, String videoid, String channelid, Integer duration, Long time) {
        this.id = id;
        this.username = username;
        this.videoid = videoid;
        this.channelid = channelid;
        this.duration = duration;
        this.time = time;
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

    public String getVideoid() {
        return videoid;
    }

    public void setVideoid(String videoid) {
        this.videoid = videoid;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
