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
@Table(name = "task_priority")
public class TaskPriority {
    @Id
    private String task;
    @Column(columnDefinition = "integer default 0")
    private Integer priority;
    @Column(columnDefinition = "integer default 0")
    private Integer state;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = " varchar(255) default ''")
    private String platform;
    @Column(columnDefinition = "integer default 0")
    private Integer time_waiting_task=0;
    public TaskPriority() {
    }

}
