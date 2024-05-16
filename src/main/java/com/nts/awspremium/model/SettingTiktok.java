package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "setting_tiktok")
public class SettingTiktok {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer max_follower;
    private Integer max_reg;
    private Integer max_bonus;
    private Integer max_day_activity;
    private Integer max_activity_24h;

    public SettingTiktok() {
    }

    public SettingTiktok(Long id, Integer max_follower, Integer max_reg, Integer max_bonus, Integer max_day_activity, Integer max_activity_24h) {
        this.id = id;
        this.max_follower = max_follower;
        this.max_reg = max_reg;
        this.max_bonus = max_bonus;
        this.max_day_activity = max_day_activity;
        this.max_activity_24h = max_activity_24h;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMax_follower() {
        return max_follower;
    }

    public void setMax_follower(Integer max_follower) {
        this.max_follower = max_follower;
    }

    public Integer getMax_reg() {
        return max_reg;
    }

    public void setMax_reg(Integer max_reg) {
        this.max_reg = max_reg;
    }

    public Integer getMax_bonus() {
        return max_bonus;
    }

    public void setMax_bonus(Integer max_bonus) {
        this.max_bonus = max_bonus;
    }

    public Integer getMax_day_activity() {
        return max_day_activity;
    }

    public void setMax_day_activity(Integer max_day_activity) {
        this.max_day_activity = max_day_activity;
    }

    public Integer getMax_activity_24h() {
        return max_activity_24h;
    }

    public void setMax_activity_24h(Integer max_activity_24h) {
        this.max_activity_24h = max_activity_24h;
    }
}
