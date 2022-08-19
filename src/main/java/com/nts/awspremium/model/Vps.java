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
    private Integer threads;
    private Integer vpsreset;
    private Integer timereset;
    private Integer dayreset;
    public Vps() {
    }

    public Integer getDayreset() {
        return dayreset;
    }

    public void setDayreset(Integer dayreset) {
        this.dayreset = dayreset;
    }

    public Vps(Integer id, String vps, String urlapi, String token, String vpsoption, Integer state, Integer running, Long timecheck, Integer threads, Integer vpsreset, Integer timereset, Integer dayreset) {
        this.id = id;
        this.vps = vps;
        this.urlapi = urlapi;
        this.token = token;
        this.vpsoption = vpsoption;
        this.state = state;
        this.running = running;
        this.timecheck = timecheck;
        this.threads = threads;
        this.vpsreset = vpsreset;
        this.timereset = timereset;
        this.dayreset = dayreset;
    }

    public Integer getTimereset() {
        return timereset;
    }

    public void setTimereset(Integer timereset) {
        this.timereset = timereset;
    }

    public Integer getVpsreset() {
        return vpsreset;
    }

    public void setVpsreset(Integer vpsreset) {
        this.vpsreset = vpsreset;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
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
