package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "videocommenthistory")
public class VideoCommentHistory {
    @Id
    private Long orderid;
    private String videoid;
    private String videotitle;
    private String channelid;
    private Integer commentstart;
    private Integer commentend;
    private Integer commenttotal;
    private Integer cancel;
    private Long insertdate;
    private Long enddate;
    private Integer commentorder;
    private Integer service;
    private Integer maxthreads;
    private Long duration;
    private String channeltitle;
    private String note;
    private String user;
    private Float price;
    private Long timecheck;
    private Integer numbh;
    public VideoCommentHistory() {
    }

    public VideoCommentHistory(Long orderid, String videoid, String videotitle, String channelid, Integer commentstart, Integer commentend, Integer commenttotal, Integer cancel, Long insertdate, Long enddate, Integer commentorder, Integer service, Integer maxthreads, Long duration, String channeltitle, String note, String user, Float price, Long timecheck, Integer numbh) {
        this.orderid = orderid;
        this.videoid = videoid;
        this.videotitle = videotitle;
        this.channelid = channelid;
        this.commentstart = commentstart;
        this.commentend = commentend;
        this.commenttotal = commenttotal;
        this.cancel = cancel;
        this.insertdate = insertdate;
        this.enddate = enddate;
        this.commentorder = commentorder;
        this.service = service;
        this.maxthreads = maxthreads;
        this.duration = duration;
        this.channeltitle = channeltitle;
        this.note = note;
        this.user = user;
        this.price = price;
        this.timecheck = timecheck;
        this.numbh = numbh;
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

    public Integer getCommentstart() {
        return commentstart;
    }

    public void setCommentstart(Integer commentstart) {
        this.commentstart = commentstart;
    }

    public Integer getCommentend() {
        return commentend;
    }

    public void setCommentend(Integer commentend) {
        this.commentend = commentend;
    }

    public Integer getCommenttotal() {
        return commenttotal;
    }

    public void setCommenttotal(Integer commenttotal) {
        this.commenttotal = commenttotal;
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

    public Integer getCommentorder() {
        return commentorder;
    }

    public void setCommentorder(Integer commentorder) {
        this.commentorder = commentorder;
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
}
