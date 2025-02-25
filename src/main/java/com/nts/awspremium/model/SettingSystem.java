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
    private Integer max_thread=0;
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

    public SettingSystem(Long id, Integer max_acc, Integer max_task, Integer max_thread, Integer max_profile, Integer max_mysql, Integer max_priority, Long update_time, Long reboot_time, Long update_pi_time, String clear_data_package, Integer time_profile, Integer time_enable_profile, String rom_version, Integer time_get_task, Integer time_waiting_task) {
        this.id = id;
        this.max_acc = max_acc;
        this.max_task = max_task;
        this.max_thread = max_thread;
        this.max_profile = max_profile;
        this.max_mysql = max_mysql;
        this.max_priority = max_priority;
        this.update_time = update_time;
        this.reboot_time = reboot_time;
        this.update_pi_time = update_pi_time;
        this.clear_data_package = clear_data_package;
        this.time_profile = time_profile;
        this.time_enable_profile = time_enable_profile;
        this.rom_version = rom_version;
        this.time_get_task = time_get_task;
        this.time_waiting_task = time_waiting_task;
    }
}
