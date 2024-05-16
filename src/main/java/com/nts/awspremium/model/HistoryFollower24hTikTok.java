package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "history_follower_24h_tiktok")
public class HistoryFollower24hTikTok {
    @Id
    private String code;
    private Long time;

    public HistoryFollower24hTikTok() {
    }

    public HistoryFollower24hTikTok(String code, Long time) {
        this.code = code;
        this.time = time;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}