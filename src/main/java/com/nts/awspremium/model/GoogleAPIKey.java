package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "googleapikey")
public class GoogleAPIKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String keyapi;
    private Long count_get;
    private Integer state;

    public GoogleAPIKey() {
    }

    public GoogleAPIKey(Long id, String keyapi, Long count_get, Integer state) {
        this.id = id;
        this.keyapi = keyapi;
        this.count_get = count_get;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return keyapi;
    }

    public void setKey(String keyapi) {
        this.keyapi = keyapi;
    }

    public Long getCount() {
        return count_get;
    }

    public void setCount(Long count_get) {
        this.count_get = count_get;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
