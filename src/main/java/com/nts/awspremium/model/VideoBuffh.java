package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "videobuffh")
public class VideoBuffh {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String videoid;
    private String videotitle;
    private String channelid;
    private Integer viewstart;
    private Integer viewend;
    private Integer mobilerate;
    private Long insertdate;
    private Long enddate;
    private Integer homerate;
    private Integer searchrate;
    private Integer suggestrate;
    private Integer directrate;
    private Integer enabled;
    private Integer timebuff;
    private Integer maxthreads;
    private Long duration;
    private Integer optionbuff;
    private String channeltitle;
    private String note;

    private Integer likerate;
    private Integer commentrate;
    private String user;
    private Integer timebufftotal;
    private Integer timebuff24h;
    private Integer viewtotal;
    private Integer view24h;
    private Long timeupdate;

    public VideoBuffh() {
    }

    public VideoBuffh(Long id, String videoid, String videotitle, String channelid, Integer viewstart, Integer viewend, Integer mobilerate, Long insertdate, Long enddate, Integer homerate, Integer searchrate, Integer suggestrate, Integer directrate, Integer enabled, Integer timebuff, Integer maxthreads, Long duration, Integer optionbuff, String channeltitle, String note, Integer likerate, Integer commentrate, String user, Integer timebufftotal, Integer timebuff24h, Integer viewtotal, Integer view24h, Long timeupdate) {
        this.id = id;
        this.videoid = videoid;
        this.videotitle = videotitle;
        this.channelid = channelid;
        this.viewstart = viewstart;
        this.viewend = viewend;
        this.mobilerate = mobilerate;
        this.insertdate = insertdate;
        this.enddate = enddate;
        this.homerate = homerate;
        this.searchrate = searchrate;
        this.suggestrate = suggestrate;
        this.directrate = directrate;
        this.enabled = enabled;
        this.timebuff = timebuff;
        this.maxthreads = maxthreads;
        this.duration = duration;
        this.optionbuff = optionbuff;
        this.channeltitle = channeltitle;
        this.note = note;
        this.likerate = likerate;
        this.commentrate = commentrate;
        this.user = user;
        this.timebufftotal = timebufftotal;
        this.timebuff24h = timebuff24h;
        this.viewtotal = viewtotal;
        this.view24h = view24h;
        this.timeupdate = timeupdate;
    }

    public Integer getTimebuff24h() {
        return timebuff24h;
    }

    public void setTimebuff24h(Integer timebuff24h) {
        this.timebuff24h = timebuff24h;
    }

    public Integer getView24h() {
        return view24h;
    }

    public void setView24h(Integer view24h) {
        this.view24h = view24h;
    }

    public Integer getTimebufftotal() {
        return timebufftotal;
    }

    public void setTimebufftotal(Integer timebufftotal) {
        this.timebufftotal = timebufftotal;
    }

    public Integer getViewtotal() {
        return viewtotal;
    }

    public void setViewtotal(Integer viewtotal) {
        this.viewtotal = viewtotal;
    }

    public Long getTimeupdate() {
        return timeupdate;
    }

    public void setTimeupdate(Long timeupdate) {
        this.timeupdate = timeupdate;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getLikerate() {
        return likerate;
    }

    public void setLikerate(Integer likerate) {
        this.likerate = likerate;
    }

    public Integer getCommentrate() {
        return commentrate;
    }

    public void setCommentrate(Integer commentrate) {
        this.commentrate = commentrate;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getOptionbuff() {
        return optionbuff;
    }

    public void setOptionbuff(Integer optionbuff) {
        this.optionbuff = optionbuff;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getMobilerate() {
        return mobilerate;
    }

    public void setMobilerate(Integer mobilerate) {
        this.mobilerate = mobilerate;
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

    public Integer getHomerate() {
        return homerate;
    }

    public void setHomerate(Integer homerate) {
        this.homerate = homerate;
    }

    public Integer getSearchrate() {
        return searchrate;
    }

    public void setSearchrate(Integer searchrate) {
        this.searchrate = searchrate;
    }

    public Integer getSuggestrate() {
        return suggestrate;
    }

    public void setSuggestrate(Integer suggestrate) {
        this.suggestrate = suggestrate;
    }

    public Integer getDirectrate() {
        return directrate;
    }

    public void setDirectrate(Integer directrate) {
        this.directrate = directrate;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getTimebuff() {
        return timebuff;
    }

    public void setTimebuff(Integer timebuff) {
        this.timebuff = timebuff;
    }

    public Integer getMaxthreads() {
        return maxthreads;
    }

    public void setMaxthreads(Integer maxthreads) {
        this.maxthreads = maxthreads;
    }

    public String getChanneltitle() {
        return channeltitle;
    }

    public void setChanneltitle(String channeltitle) {
        this.channeltitle = channeltitle;
    }
}
