package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "setting_facebook")
public class SettingFacebook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "integer default 0")
    private Integer max_follower;
    @Column(columnDefinition = "integer default 0")
    private Integer max_like;
    @Column(columnDefinition = "integer default 0")
    private Integer max_day_activity;
    @Column(columnDefinition = "integer default 0")
    private Integer max_activity_24h;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;

    public SettingFacebook() {
    }

}
