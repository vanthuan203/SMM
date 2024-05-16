package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "history_follower_tiktok")
public class HistoryFollowerTikTok {
    @Id
    private String username;
    private String list_tiktok_id;
    private Long time_update;

    public HistoryFollowerTikTok() {
    }

    public HistoryFollowerTikTok(String username, String list_tiktok_id, Long time_update) {
        this.username = username;
        this.list_tiktok_id = list_tiktok_id;
        this.time_update = time_update;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getList_tiktok_id() {
        return list_tiktok_id;
    }

    public void setList_tiktok_id(String list_tiktok_id) {
        this.list_tiktok_id = list_tiktok_id;
    }

    public Long getTime_update() {
        return time_update;
    }

    public void setTime_update(Long time_update) {
        this.time_update = time_update;
    }
}