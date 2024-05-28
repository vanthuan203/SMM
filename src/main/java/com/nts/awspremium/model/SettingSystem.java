package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "setting_system")
public class SettingSystem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "integer default 0")
    private Integer max_acc;
    @Column(columnDefinition = "integer default 0")
    private Integer max_task;
    @Column(columnDefinition = "integer default 0")
    private Integer max_profile;

    public SettingSystem() {
    }

}
