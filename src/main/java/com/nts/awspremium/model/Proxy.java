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
    private String ipv4;
    private Integer running;
    private String typeproxy;
    private String vps;

    public Proxy() {
    }

    public Proxy(Integer id, String proxy, Integer state, Long timeget, String ipv4, Integer running, String typeproxy, String vps) {
        this.id = id;
        this.proxy = proxy;
        this.state = state;
        this.timeget = timeget;
        this.ipv4 = ipv4;
        this.running = running;
        this.typeproxy = typeproxy;
        this.vps = vps;
    }

    public String getVps() {
        return vps;
    }

    public void setVps(String vps) {
        this.vps = vps;
    }

    public String getTypeproxy() {
        return typeproxy;
    }

    public void setTypeproxy(String typeproxy) {
        this.typeproxy = typeproxy;
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

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }
}
