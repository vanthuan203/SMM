package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "accountchange")
public class AccountChange {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String geo;
    private String name;
    private Integer running;
    private Long time;
    private String note;
    private Integer priority=0;
    public AccountChange() {
    }

    public AccountChange(Long id, String geo, String name, Integer running, Long time, String note, Integer priority) {
        this.id = id;
        this.geo = geo;
        this.name = name;
        this.running = running;
        this.time = time;
        this.note = note;
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
