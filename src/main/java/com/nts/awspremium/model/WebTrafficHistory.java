package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "webtraffichistory")
public class WebTrafficHistory {
    @Id
    private Long orderid;
    private String token="";
    private String link;
    private String keywords;

    private Integer traffictotal;
    private Integer cancel;
    private Long insertdate;
    private Long enddate;
    private Integer trafficorder;
    private Integer service;
    private Integer maxthreads;
    private String note;
    private String user;
    private Float price;
    private Long timecheck=0L;
    private Integer numbh=0;
    private Integer refund=0;
    private Integer waitbh=0;
    private Long timecheckbh=0L;

    private Long timestart=0L;
    public WebTrafficHistory() {
    }

    public WebTrafficHistory(Long orderid, String token, String link, String keywords, Integer traffictotal, Integer cancel, Long insertdate, Long enddate, Integer trafficorder, Integer service, Integer maxthreads, String note, String user, Float price, Long timecheck, Integer numbh, Integer refund, Integer waitbh, Long timecheckbh, Long timestart) {
        this.orderid = orderid;
        this.token = token;
        this.link = link;
        this.keywords = keywords;
        this.traffictotal = traffictotal;
        this.cancel = cancel;
        this.insertdate = insertdate;
        this.enddate = enddate;
        this.trafficorder = trafficorder;
        this.service = service;
        this.maxthreads = maxthreads;
        this.note = note;
        this.user = user;
        this.price = price;
        this.timecheck = timecheck;
        this.numbh = numbh;
        this.refund = refund;
        this.waitbh = waitbh;
        this.timecheckbh = timecheckbh;
        this.timestart = timestart;
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

    public Integer getTraffictotal() {
        return traffictotal;
    }

    public void setTraffictotal(Integer traffictotal) {
        this.traffictotal = traffictotal;
    }


    public Integer getCancel() {
        return cancel;
    }

    public void setCancel(Integer cancel) {
        this.cancel = cancel;
    }

    public Long getInsertdate() {
        return insertdate;
    }

    public void setInsertdate(Long insertdate) {
        this.insertdate = insertdate;
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

    public Integer getService() {
        return service;
    }

    public void setService(Integer service) {
        this.service = service;
    }

    public Integer getMaxthreads() {
        return maxthreads;
    }

    public void setMaxthreads(Integer maxthreads) {
        this.maxthreads = maxthreads;
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

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Long getTimecheck() {
        return timecheck;
    }

    public void setTimecheck(Long timecheck) {
        this.timecheck = timecheck;
    }

    public Integer getNumbh() {
        return numbh;
    }

    public void setNumbh(Integer numbh) {
        this.numbh = numbh;
    }

    public Integer getRefund() {
        return refund;
    }

    public void setRefund(Integer refund) {
        this.refund = refund;
    }

    public Integer getWaitbh() {
        return waitbh;
    }

    public void setWaitbh(Integer waitbh) {
        this.waitbh = waitbh;
    }

    public Long getTimecheckbh() {
        return timecheckbh;
    }

    public void setTimecheckbh(Long timecheckbh) {
        this.timecheckbh = timecheckbh;
    }

    public Long getTimestart() {
        return timestart;
    }

    public void setTimestart(Long timestart) {
        this.timestart = timestart;
    }
}
