package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name="proxy")
public class Proxy {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String proxy;
    private Integer state;
    private Long timeget;

    public Proxy() {
    }

    public Proxy(Integer id, String proxy, Integer state, Long timeget) {
        this.id = id;
        this.proxy = proxy;
        this.state = state;
        this.timeget = timeget;
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "id=" + id +
                ", proxy='" + proxy + '\'' +
                ", state=" + state +
                ", timeget=" + timeget +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getTimeget() {
        return timeget;
    }

    public void setTimeget(Long timeget) {
        this.timeget = timeget;
    }
}
