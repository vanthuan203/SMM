package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "videobuffhhistory")
public class VideoBuffhHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String videoid;
    private String videotitle;
    private String channelid;
    private Integer viewstart;
    private Integer viewend;
    private Integer timebuffend;
    private Integer viewbuffend;
    private Integer cancel;
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
    private Integer price;
    public VideoBuffhHistory() {
    }

    public VideoBuffhHistory(Long id, String videoid, String videotitle, String channelid, Integer viewstart, Integer viewend, Integer timebuffend, Integer viewbuffend, Integer cancel, Integer mobilerate, Long insertdate, Long enddate, Integer homerate, Integer searchrate, Integer suggestrate, Integer directrate, Integer enabled, Integer timebuff, Integer maxthreads, Long duration, Integer optionbuff, String channeltitle, String note, Integer likerate, Integer commentrate, String user, Integer price) {
        this.id = id;
        this.videoid = videoid;
        this.videotitle = videotitle;
        this.channelid = channelid;
        this.viewstart = viewstart;
        this.viewend = viewend;
        this.timebuffend = timebuffend;
        this.viewbuffend = viewbuffend;
        this.cancel = cancel;
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
        this.price = price;
    }

    public Integer getPrice() {
        return price;
    }
    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getTimebuffend() {
        return timebuffend;
    }

    public void setTimebuffend(Integer timebuffend) {
        this.timebuffend = timebuffend;
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
