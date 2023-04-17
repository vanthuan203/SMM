package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "videocomment")
public class VideoComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderid;
    private String videoid;
    private String videotitle;
    private String channelid;
    private String channeltitle;
    private Integer commentstart;
    private Long insertdate;
    private Integer maxthreads;
    private Long duration;
    private Integer service;
    private String note;
    private String user;
    private Integer enddate;
    private Integer commentorder;
    private Integer commenttotal;
    private String listcomment;
    private Long timeupdate;
    private Float price;
    private Integer valid;

    public VideoComment() {
    }

    public VideoComment(Long orderid, String videoid, String videotitle, String channelid, String channeltitle, Integer commentstart, Long insertdate, Integer maxthreads, Long duration, Integer service, String note, String user, Integer enddate, Integer commentorder, Integer commenttotal, String listcomment, Long timeupdate, Float price, Integer valid) {
        this.orderid = orderid;
        this.videoid = videoid;
        this.videotitle = videotitle;
        this.channelid = channelid;
        this.channeltitle = channeltitle;
        this.commentstart = commentstart;
        this.insertdate = insertdate;
        this.maxthreads = maxthreads;
        this.duration = duration;
        this.service = service;
        this.note = note;
        this.user = user;
        this.enddate = enddate;
        this.commentorder = commentorder;
        this.commenttotal = commenttotal;
        this.listcomment = listcomment;
        this.timeupdate = timeupdate;
        this.price = price;
        this.valid = valid;
    }

    public String getListcomment() {
        return listcomment;
    }

    public void setListcomment(String listcomment) {
        this.listcomment = listcomment;
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

    public String getChanneltitle() {
        return channeltitle;
    }

    public void setChanneltitle(String channeltitle) {
        this.channeltitle = channeltitle;
    }

    public Integer getCommentstart() {
        return commentstart;
    }

    public void setCommentstart(Integer commentstart) {
        this.commentstart = commentstart;
    }

    public Long getInsertdate() {
        return insertdate;
    }

    public void setInsertdate(Long insertdate) {
        this.insertdate = insertdate;
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

    public Integer getEnddate() {
        return enddate;
    }

    public void setEnddate(Integer enddate) {
        this.enddate = enddate;
    }

    public Integer getCommentorder() {
        return commentorder;
    }

    public void setCommentorder(Integer commentorder) {
        this.commentorder = commentorder;
    }

    public Integer getCommenttotal() {
        return commenttotal;
    }

    public void setCommenttotal(Integer commenttotal) {
        this.commenttotal = commenttotal;
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
}
