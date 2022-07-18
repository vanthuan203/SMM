package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "vps")
public class Vps {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String vps;
    private String urlapi;
    private String token;
    private String vpsoption;
    private Integer state;
    private Integer running;
    private Long timecheck;

    public Vps() {
    }

    public Vps(Integer id, String vps, String urlapi, String token, String vpsoption, Integer state, Integer running, Long timecheck) {
        this.id = id;
        this.vps = vps;
        this.urlapi = urlapi;
        this.token = token;
        this.vpsoption = vpsoption;
        this.state = state;
        this.running = running;
        this.timecheck = timecheck;
    }

    @Override
    public String toString() {
        return "Vps{" +
                "id=" + id +
                ", vps='" + vps + '\'' +
                ", urlapi='" + urlapi + '\'' +
                ", token='" + token + '\'' +
                ", vpsoption='" + vpsoption + '\'' +
                ", state=" + state +
                ", running=" + running +
                ", timecheck=" + timecheck +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVps() {
        return vps;
    }

    public void setVps(String vps) {
        this.vps = vps;
    }

    public String getUrlapi() {
        return urlapi;
    }

    public void setUrlapi(String urlapi) {
        this.urlapi = urlapi;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getVpsoption() {
        return vpsoption;
    }

    public void setVpsoption(String vpsoption) {
        this.vpsoption = vpsoption;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public Long getTimecheck() {
        return timecheck;
    }

    public void setTimecheck(Long timecheck) {
        this.timecheck = timecheck;
    }
}
