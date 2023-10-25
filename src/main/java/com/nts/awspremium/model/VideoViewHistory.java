package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "videoviewhistory")
public class VideoViewHistory {
    @Id
    private Long orderid;
    private String videoid;
    private String videotitle;
    private String channelid;
    private Integer viewstart;
    private Integer viewend=-1;
    private Integer viewtotal;
    private Integer timetotal;
    private Integer cancel;
    private Long insertdate;
    private Long enddate;
    private Integer vieworder;
    private Integer service;
    private Integer maxthreads;
    private Long duration;
    private String channeltitle;
    private String note;
    private String user;
    private Float price;
    private Long timecheck=0L;
    private Integer numbh=0;
    private Integer refund=0;
    private Integer waitbh=0;
    private Long timecheckbh=0L;

    private Long timestart=0L;
    public VideoViewHistory() {
    }

    public VideoViewHistory(Long orderid, String videoid, String videotitle, String channelid, Integer viewstart, Integer viewend, Integer viewtotal, Integer timetotal, Integer cancel, Long insertdate, Long enddate, Integer vieworder, Integer service, Integer maxthreads, Long duration, String channeltitle, String note, String user, Float price, Long timecheck, Integer numbh, Integer refund, Integer waitbh, Long timecheckbh, Long timestart) {
        this.orderid = orderid;
        this.videoid = videoid;
        this.videotitle = videotitle;
        this.channelid = channelid;
        this.viewstart = viewstart;
        this.viewend = viewend;
        this.viewtotal = viewtotal;
        this.timetotal = timetotal;
        this.cancel = cancel;
        this.insertdate = insertdate;
        this.enddate = enddate;
        this.vieworder = vieworder;
        this.service = service;
        this.maxthreads = maxthreads;
        this.duration = duration;
        this.channeltitle = channeltitle;
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

    public Integer getTimetotal() {
        return timetotal;
    }

    public void setTimetotal(Integer timetotal) {
        this.timetotal = timetotal;
    }

    public Long getTimecheckbh() {
        return timecheckbh;
    }

    public void setTimecheckbh(Long timecheckbh) {
        this.timecheckbh = timecheckbh;
    }

    public Integer getWaitbh() {
        return waitbh;
    }

    public void setWaitbh(Integer waitbh) {
        this.waitbh = waitbh;
    }

    public Integer getRefund() {
        return refund;
    }

    public void setRefund(Integer refund) {
        this.refund = refund;
    }

    public Integer getViewtotal() {
        return viewtotal;
    }

    public void setViewtotal(Integer viewtotal) {
        this.viewtotal = viewtotal;
    }

    public VideoViewHistory(Long orderid) {
        this.orderid = orderid;
    }

    public Integer getVieworder() {
        return vieworder;
    }

    public void setVieworder(Integer vieworder) {
        this.vieworder = vieworder;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public String getVideoid() {
        return videoid;
    }

    public void setVideoid(String videoid) {
        this.videoid = videoid;
    }

    public String getVideotitle() {
        return videotitle;
    }

    public void setVideotitle(String videotitle) {
        this.videotitle = videotitle;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public Integer getViewstart() {
        return viewstart;
    }

    public void setViewstart(Integer viewstart) {
        this.viewstart = viewstart;
    }

    public Integer getViewend() {
        return viewend;
    }

    public void setViewend(Integer viewend) {
        this.viewend = viewend;
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

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getChanneltitle() {
        return channeltitle;
    }

    public void setChanneltitle(String channeltitle) {
        this.channeltitle = channeltitle;
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

    public Long getTimestart() {
        return timestart;
    }

    public void setTimestart(Long timestart) {
        this.timestart = timestart;
    }
}
