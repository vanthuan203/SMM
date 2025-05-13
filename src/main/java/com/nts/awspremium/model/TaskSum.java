package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "task_sum")
public class TaskSum {
    @Id
    private String id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String platform;
    @Column(columnDefinition = "varchar(255) default ''")
    private String task;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "TINYINT default 1")
    private Boolean status;
    @Column(columnDefinition = "TINYINT default 1")
    private Boolean success;
    @Column(columnDefinition = "varchar(255) default ''")
    private String ip;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id",referencedColumnName = "profile_id",updatable = true,insertable = true)
    private ProfileTask profileTask;
}