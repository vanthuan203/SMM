package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "device_error_log")
public class DeviceErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String device_id;
    private String error;
    public DeviceErrorLog() {
    }

    public DeviceErrorLog(Long id, String device_id, String error) {
        this.id = id;
        this.device_id = device_id;
        this.error = error;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
