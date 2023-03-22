package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "service")
public class Service {
    @Id
    private Integer service;
    private String name;
    private String type;
    private String category;
    private Float rate;
    private Integer min;
    private Integer max;
    private Integer thread;
    private String note;
    private String geo;
    public Service() {
    }


    public Service(Integer service, String name, String type, String category, Float rate, Integer min, Integer max, Integer thread, String note, String geo) {
        this.service = service;
        this.name = name;
        this.type = type;
        this.category = category;
        this.rate = rate;
        this.min = min;
        this.max = max;
        this.thread = thread;
        this.note = note;
        this.geo = geo;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getService() {
        return service;
    }

    public void setService(Integer service) {
        this.service = service;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }
}
