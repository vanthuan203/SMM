package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "history_view_youtube")
public class HistoryViewYoutube {
    @Id
    private String username;
    private String list_video;
    private Long time_update;

    public HistoryViewYoutube() {
    }

    public HistoryViewYoutube(String username, String list_video, Long time_update) {
        this.username = username;
        this.list_video = list_video;
        this.time_update = time_update;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getList_video() {
        return list_video;
    }

    public void setList_video(String list_video) {
        this.list_video = list_video;
    }

    public Long getTime_update() {
        return time_update;
    }

    public void setTime_update(Long time_update) {
        this.time_update = time_update;
    }
}