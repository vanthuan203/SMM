package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "videoview")
public class VideoView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderid;
    private String videoid;
    private String videotitle;
    private String channelid;
    private String channeltitle;
    private Integer viewstart;
    private Long insertdate;
    private Long timestart;
    private Integer minstart;
    private Integer maxthreads;
    private Integer threadset;
    private Long duration;
    private Integer service;
    private String note;
    private String user;
    private Integer enddate;
    private Integer vieworder;
    private Integer viewtotal;
    private Integer timetotal;
    private Integer view24h;
    private Long timeupdate;
    private Float price;
    private Integer valid;
    private Integer priority=0;
    private Integer speedup=0;

    private String link="";

    public VideoView() {
    }

    public VideoView(Long orderid, String videoid, String videotitle, String channelid, String channeltitle, Integer viewstart, Long insertdate, Long timestart, Integer minstart, Integer maxthreads, Integer threadset, Long duration, Integer service, String note, String user, Integer enddate, Integer vieworder, Integer viewtotal, Integer timetotal, Integer view24h, Long timeupdate, Float price, Integer valid, Integer priority, Integer speedup, String link) {
        this.orderid = orderid;
        this.videoid = videoid;
        this.videotitle = videotitle;
        this.channelid = channelid;
        this.channeltitle = channeltitle;
        this.viewstart = viewstart;
        this.insertdate = insertdate;
        this.timestart = timestart;
        this.minstart = minstart;
        this.maxthreads = maxthreads;
        this.threadset = threadset;
        this.duration = duration;
        this.service = service;
        this.note = note;
        this.user = user;
        this.enddate = enddate;
        this.vieworder = vieworder;
        this.viewtotal = viewtotal;
        this.timetotal = timetotal;
        this.view24h = view24h;
        this.timeupdate = timeupdate;
        this.price = price;
        this.valid = valid;
        this.priority = priority;
        this.speedup = speedup;
        this.link = link;
    }

    public Integer getTimetotal() {
        return timetotal;
    }

    public void setTimetotal(Integer timetotal) {
        this.timetotal = timetotal;
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
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

    public String getChanneltitle() {
        return channeltitle;
    }

    public void setChanneltitle(String channeltitle) {
        this.channeltitle = channeltitle;
    }

    public Integer getViewstart() {
        return viewstart;
    }

    public void setViewstart(Integer viewstart) {
        this.viewstart = viewstart;
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

    public Integer getViewtotal() {
        return viewtotal;
    }

    public void setViewtotal(Integer viewtotal) {
        this.viewtotal = viewtotal;
    }

    public Integer getView24h() {
        return view24h;
    }

    public void setView24h(Integer view24h) {
        this.view24h = view24h;
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

    public Long getTimestart() {
        return timestart;
    }

    public void setTimestart(Long timestart) {
        this.timestart = timestart;
    }

    public Integer getMinstart() {
        return minstart;
    }

    public void setMinstart(Integer minstart) {
        this.minstart = minstart;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getThreadset() {
        return threadset;
    }

    public void setThreadset(Integer threadset) {
        this.threadset = threadset;
    }

    public Integer getSpeedup() {
        return speedup;
    }

    public void setSpeedup(Integer speedup) {
        this.speedup = speedup;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
