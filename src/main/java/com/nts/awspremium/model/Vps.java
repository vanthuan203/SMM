package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "vps")
public class Vps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String ipv4;
    private Integer countipv4;
    private String namevps;
    private Integer changefinger;
    private Integer ext;
    private Integer get_account;
    public Vps() {
    }

    public Integer getDayreset() {
        return dayreset;
    }

    public String getNamevps() {
        return namevps;
    }

    public void setNamevps(String namevps) {
        this.namevps = namevps;
    }

    public void setDayreset(Integer dayreset) {
        this.dayreset = dayreset;
    }

    public Vps(Integer id, String vps, String urlapi, String token, String vpsoption, Integer state, Integer running, Long timecheck, Integer threads, Integer vpsreset, Integer timereset, Integer dayreset, String ipv4, Integer countipv4, String namevps, Integer changefinger, Integer ext, Integer get_account) {
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
        this.ipv4 = ipv4;
        this.countipv4 = countipv4;
        this.namevps = namevps;
        this.changefinger = changefinger;
        this.ext = ext;
        this.get_account = get_account;
    }

    public Integer getGet_account() {
        return get_account;
    }

    public void setGet_account(Integer get_account) {
        this.get_account = get_account;
    }

    public Integer getExt() {
        return ext;
    }

    public void setExt(Integer ext) {
        this.ext = ext;
    }

    public Integer getChangefinger() {
        return changefinger;
    }

    public void setChangefinger(Integer changefinger) {
        this.changefinger = changefinger;
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

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public Integer getCountipv4() {
        return countipv4;
    }

    public void setCountipv4(Integer countipv4) {
        this.countipv4 = countipv4;
    }
}
