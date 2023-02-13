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
    private Integer viewend;
    private Integer viewbuffend;
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
    private Integer price;
    private Long timecheck;
    private Integer numbh;
    public VideoViewHistory() {
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

    public Integer getViewbuffend() {
        return viewbuffend;
    }

    public void setViewbuffend(Integer viewbuffend) {
        this.viewbuffend = viewbuffend;
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

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
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
}
