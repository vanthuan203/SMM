package com.nts.awspremium.model;

import javax.persistence.*;

@Table(name = "historytrafficsum")
@Entity
public class HistoryTraficSum {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private Long orderid;
    private String keyword;
    private String device;
    private Integer duration;
    private Long time;
    private Integer ranking;
    public HistoryTraficSum() {
    }

    public HistoryTraficSum(Long id, String username, Long orderid, String keyword, String device, Integer duration, Long time, Integer ranking) {
        this.id = id;
        this.username = username;
        this.orderid = orderid;
        this.keyword = keyword;
        this.device = device;
        this.duration = duration;
        this.time = time;
        this.ranking = ranking;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getRank() {
        return ranking;
    }

    public void setRank(Integer ranking) {
        this.ranking = ranking;
    }
}
