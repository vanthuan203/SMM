package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "proxyhistory")
public class ProxyHistory {
    @Id
    private Long id;
    private String proxy;
    private String ipv4;
    private int state;

    public ProxyHistory() {
    }

    public ProxyHistory(Long id, String proxy, String ipv4, int state) {
        this.id = id;
        this.proxy = proxy;
        this.ipv4 = ipv4;
        this.state = state;
    }

    @Override
    public String toString() {
        return "ProxyHistory{" +
                "id=" + id +
                ", proxy='" + proxy + '\'' +
                ", ipv4='" + ipv4 + '\'' +
                ", state=" + state +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }
}
