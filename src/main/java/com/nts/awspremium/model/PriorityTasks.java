package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "priority_task")
public class PriorityTasks {
    @Id
    private String task;
    private Integer priority;
    private Integer state;
    public PriorityTasks() {
    }

    public PriorityTasks(String task, Integer priority, Integer state) {
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
