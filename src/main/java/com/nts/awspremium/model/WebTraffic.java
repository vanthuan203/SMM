package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "webtraffic")
public class WebTraffic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderid;
    private String token="";
    private String link;
    private String keywords;
    private Long insertdate;
    private Long timestart;
    private Integer maxthreads;
    private Integer service;
    private String note;
    private String user;
    private Long enddate;
    private Integer trafficorder;
    private Integer traffictotal=0;
    private Integer traffic24h=0;
    private Integer maxtraffic24h=0;
    private Long timeupdate=0L;
    private Float price;
    private Integer valid=1;
    private Integer priority=0;
    private Integer speedup=0;

    public WebTraffic() {
    }

    public WebTraffic(Long orderid, String token, String link, String keywords, Long insertdate, Long timestart, Integer maxthreads, Integer service, String note, String user, Long enddate, Integer trafficorder, Integer traffictotal, Integer traffic24h, Integer maxtraffic24h, Long timeupdate, Float price, Integer valid, Integer priority, Integer speedup) {
        this.orderid = orderid;
        this.token = token;
        this.link = link;
        this.keywords = keywords;
        this.insertdate = insertdate;
        this.timestart = timestart;
        this.maxthreads = maxthreads;
        this.service = service;
        this.note = note;
        this.user = user;
        this.enddate = enddate;
        this.trafficorder = trafficorder;
        this.traffictotal = traffictotal;
        this.traffic24h = traffic24h;
        this.maxtraffic24h = maxtraffic24h;
        this.timeupdate = timeupdate;
        this.price = price;
        this.valid = valid;
        this.priority = priority;
        this.speedup = speedup;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Long getInsertdate() {
        return insertdate;
    }

    public void setInsertdate(Long insertdate) {
        this.insertdate = insertdate;
    }

    public Long getTimestart() {
        return timestart;
    }

    public void setTimestart(Long timestart) {
        this.timestart = timestart;
    }

    public Integer getMaxthreads() {
        return maxthreads;
    }

    public void setMaxthreads(Integer maxthreads) {
        this.maxthreads = maxthreads;
    }

    public Integer getService() {
        return service;
    }

    public void setService(Integer service) {
        this.service = service;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getEnddate() {
        return enddate;
    }

    public void setEnddate(Long enddate) {
        this.enddate = enddate;
    }

    public Integer getTrafficorder() {
        return trafficorder;
    }

    public void setTrafficorder(Integer trafficorder) {
        this.trafficorder = trafficorder;
    }

    public Integer getTraffictotal() {
        return traffictotal;
    }

    public void setTraffictotal(Integer traffictotal) {
        this.traffictotal = traffictotal;
    }

    public Integer getTraffic24h() {
        return traffic24h;
    }

    public void setTraffic24h(Integer traffic24h) {
        this.traffic24h = traffic24h;
    }

    public Long getTimeupdate() {
        return timeupdate;
    }

    public void setTimeupdate(Long timeupdate) {
        this.timeupdate = timeupdate;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getSpeedup() {
        return speedup;
    }

    public void setSpeedup(Integer speedup) {
        this.speedup = speedup;
    }

    public Integer getMaxtraffic24h() {
        return maxtraffic24h;
    }

    public void setMaxtraffic24h(Integer maxtraffic24h) {
        this.maxtraffic24h = maxtraffic24h;
    }
}
