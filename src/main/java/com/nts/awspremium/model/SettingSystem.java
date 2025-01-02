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
    @Column(columnDefinition = "integer default 0")
    private Integer max_mysql;
    @Column(columnDefinition = "integer default 0")
    private Integer max_priority;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "bigint default 30")
    private Long reboot_time;
    @Column(columnDefinition = "bigint default 45")
    private Long update_pi_time;
    @Column(columnDefinition = "varchar(255) default ''")
    private String clear_data_package="";
    @Column(columnDefinition = "integer default 60")
    private Integer time_profile;
    @Column(columnDefinition = "integer default 4")
    private Integer time_enable_profile;
    @Column(columnDefinition = "varchar(255) default ''")
    private String rom_version;
    @Column(columnDefinition = "integer default 0")
    private Integer time_get_task;
    @Column(columnDefinition = "integer default 0")
    private Integer time_waiting_task=0;
    public SettingSystem() {
    }

}
