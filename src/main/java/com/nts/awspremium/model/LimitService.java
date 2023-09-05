package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "limitservice")
public class LimitService {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer service;
    private String user;
    private Integer maxorder;

    public LimitService() {
    }

    public LimitService(Long id, Integer service, String user, Integer maxorder) {
        this.id = id;
        this.service = service;
        this.user = user;
        this.maxorder = maxorder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getService() {
        return service;
    }

    public void setService(Integer service) {
        this.service = service;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getMaxorder() {
        return maxorder;
    }

    public void setMaxorder(Integer maxorder) {
        this.maxorder = maxorder;
    }
}
