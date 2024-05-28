package com.nts.awspremium.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "task_priority")
public class TaskPriority {
    @Id
    private String task;
    @Column(columnDefinition = "integer default 0")
    private Integer priority;
    @Column(columnDefinition = "integer default 0")
    private Integer state;
    public TaskPriority() {
    }

    public TaskPriority(String task, Integer priority, Integer state) {
        this.task = task;
        this.priority = priority;
        this.state = state;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
