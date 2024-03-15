package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "activity_tiktok")
public class ActivityTikTok {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private Float time_update;
    public ActivityTikTok() {
    }

    public ActivityTikTok(Long id, String username, Float time_update) {
        this.id = id;
        this.username = username;
        this.time_update = time_update;
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

    public Float getTime_update() {
        return time_update;
    }

    public void setTime_update(Float time_update) {
        this.time_update = time_update;
    }
}
