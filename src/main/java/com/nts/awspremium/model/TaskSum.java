package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

}