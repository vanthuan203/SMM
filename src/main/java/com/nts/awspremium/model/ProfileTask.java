package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "profile_task")
public class ProfileTask implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String profile_id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String account_id;
    @Column(columnDefinition = "bigint default 0")
    private Long add_time;
    @Column(columnDefinition = "bigint default 0")
    private Long get_time;
    @Column(columnDefinition = "bigint default 0")
    private Long online_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long google_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "bigint default 0")
    private Long enabled_time;
    @Column(columnDefinition = "bigint default 0")
    private Long clear_data_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long update_pi_time=0L;
    @Column(columnDefinition = "varchar(255) default ''")
    private String task;
    @Column(columnDefinition = "varchar(255) default ''")
    private String task_list;
    @Column(columnDefinition = "varchar(255) default ''")
    private String task_key;
    @Column(columnDefinition = "integer default 0")
    private Integer account_level;
    @Column(columnDefinition = "integer default 0")
    private Integer enabled=0;
    @Column(columnDefinition = "integer default 0")
    private Integer running;
    @Column(columnDefinition = "integer default -1")
    private Integer state=0;
    @Column(columnDefinition = "integer default 0")
    private Integer task_index;
    @Column(columnDefinition = "integer default 0")
    private Integer request_index=0;
    @Column(columnDefinition = "integer default 0")
    private Integer register_index=0;
    @Column(columnDefinition = "integer default 1")
    private Integer valid=1;
    @Column(columnDefinition = "integer default 0")
    private Integer reboot=0;
    @Column(columnDefinition = "bigint default 0")
    private Long order_id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String platform;
    @Column(columnDefinition = "bigint default 0")
    private Long tiktok_lite_version;
    @Column(columnDefinition = "integer default 0")
    private Integer clear_data=0;
    @Column(columnDefinition = "integer default 0")
    private Integer update_pi=0;
    @Column(columnDefinition = "integer default 0")
    private Integer add_proxy=0;
    @Column(columnDefinition = "integer default 0")
    private Integer dis_proxy=0;
    @Column(columnDefinition = "varchar(255) default ''")
    private String proxy="";
    @Column(columnDefinition = "bigint default 0")
    private Long task_time=0L;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id",referencedColumnName = "device_id",updatable = true,insertable = true)
    private Device device;
    @Column(columnDefinition = "varchar(1555) default ''")
    private String note="";
}
