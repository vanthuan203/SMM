package com.nts.awspremium.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "historyview")
public class HistoryView {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String channelid;
    private Integer duration;
    private Long time;

    public HistoryView() {
    }

    public HistoryView(Long id, String channelid, Integer duration, Long time) {
        this.id = id;
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
