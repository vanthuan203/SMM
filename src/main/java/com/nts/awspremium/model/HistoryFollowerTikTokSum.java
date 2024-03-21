package com.nts.awspremium.model;

import javax.persistence.*;

@Table(name = "history_follower_tiktok_sum")
@Entity
public class HistoryFollowerTikTokSum {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private String tiktok_id;
    private Long time;
    public HistoryFollowerTikTokSum() {
    }

    public HistoryFollowerTikTokSum(Long id, String username, String tiktok_id, Long time) {
        this.id = id;
        this.username = username;
        this.tiktok_id = tiktok_id;
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

    public String getTiktok_id() {
        return tiktok_id;
    }

    public void setTiktok_id(String tiktok_id) {
        this.tiktok_id = tiktok_id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
