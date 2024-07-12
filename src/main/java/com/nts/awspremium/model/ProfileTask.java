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
    private Long get_time;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "varchar(255) default ''")
    private String task;
    @Column(columnDefinition = "varchar(255) default ''")
    private String task_list;
    @Column(columnDefinition = "varchar(255) default ''")
    private String task_key;
    @Column(columnDefinition = "integer default 0")
    private Integer account_level;
    @Column(columnDefinition = "integer default 0")
    private Integer running;
    @Column(columnDefinition = "integer default -1")
    private Integer state;
    @Column(columnDefinition = "integer default 0")
    private Integer task_index;
    @Column(columnDefinition = "bigint default 0")
    private Long order_id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String platform;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id",referencedColumnName = "device_id",updatable = true,insertable = true)
    private Device device;
}
