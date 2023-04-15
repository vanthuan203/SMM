package com.nts.awspremium.model;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;

@Entity
@Table(name="ipv4")
public class IpV4 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String ipv4;
    private Integer state;
    private Long timecheck;
    private Integer timereset;
    private String vps;
    private Integer vspcount;
    private Integer usercount;

    private Integer cron;
    private Integer numcheck;

    public IpV4() {
    }

    public IpV4(long id, String ipv4, Integer state, Long timecheck, Integer timereset, String vps, Integer vspcount, Integer usercount, Integer cron, Integer numcheck) {
        this.id = id;
        this.ipv4 = ipv4;
        this.state = state;
        this.timecheck = timecheck;
        this.timereset = timereset;
        this.vps = vps;
        this.vspcount = vspcount;
        this.usercount = usercount;
        this.cron = cron;
        this.numcheck = numcheck;
    }

    public Integer getNumcheck() {
        return numcheck;
    }

    public void setNumcheck(Integer numcheck) {
        this.numcheck = numcheck;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getTimecheck() {
        return timecheck;
    }

    public void setTimecheck(Long timecheck) {
        this.timecheck = timecheck;
    }

    public Integer getTimereset() {
        return timereset;
    }

    public void setTimereset(Integer timereset) {
        this.timereset = timereset;
    }

    public String getVps() {
        return vps;
    }

    public void setVps(String vps) {
        this.vps = vps;
    }

    public Integer getVspcount() {
        return vspcount;
    }

    public void setVspcount(Integer vspcount) {
        this.vspcount = vspcount;
    }

    public Integer getUsercount() {
        return usercount;
    }

    public void setUsercount(Integer usercount) {
        this.usercount = usercount;
    }

    public Integer getCron() {
        return cron;
    }

    public void setCron(Integer cron) {
        this.cron = cron;
    }
}
