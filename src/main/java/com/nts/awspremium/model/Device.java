package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "device")
public class Device {
    @Id
    private String device_id;
    private String name;
    private String box_name;
    private Long time_update;
    private Integer state;
    public Device() {
    }

    public Device(String device_id, String name, String box_name, Long time_update, Integer state) {
        this.device_id = device_id;
        this.name = name;
        this.box_name = box_name;
        this.time_update = time_update;
        this.state = state;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBox_name() {
        return box_name;
    }

    public void setBox_name(String box_name) {
        this.box_name = box_name;
    }

    public Long getTime_update() {
        return time_update;
    }

    public void setTime_update(Long time_update) {
        this.time_update = time_update;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
