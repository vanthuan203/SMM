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
    private Integer max_activity;

    public SettingTiktok() {
    }

    public SettingTiktok(Long id, Integer max_follower, Integer max_reg, Integer max_bonus, Integer max_activity) {
        this.id = id;
        this.max_follower = max_follower;
        this.max_reg = max_reg;
        this.max_bonus = max_bonus;
        this.max_activity = max_activity;
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

    public Integer getMax_activity() {
        return max_activity;
    }

    public void setMax_activity(Integer max_activity) {
        this.max_activity = max_activity;
    }
}
