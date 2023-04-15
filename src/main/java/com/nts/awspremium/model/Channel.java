package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "channel")
public class Channel{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String channelid;
    private Integer premiumrate;
    private Integer mobilerate;
    private Long insertdate;
    private Integer homerate;
    private Integer searchrate;
    private Integer suggestrate;
    private Integer directrate;
    private Integer enabled;
    private Integer viewpercent;

    private Integer maxthreads;
    private String title;

    public Channel() {
    }

    public Channel(Long id, String channelid, Integer premiumrate, Integer mobilerate, Long insertdate, Integer homerate, Integer searchrate, Integer suggestrate, Integer directrate, Integer enabled, Integer viewpercent, Integer maxthreads, String title) {
        this.id = id;
        this.channelid = channelid;
        this.premiumrate = premiumrate;
        this.mobilerate = mobilerate;
        this.insertdate = insertdate;
        this.homerate = homerate;
        this.searchrate = searchrate;
        this.suggestrate = suggestrate;
        this.directrate = directrate;
        this.enabled = enabled;
        this.viewpercent = viewpercent;
        this.maxthreads = maxthreads;
        this.title = title;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", channelid='" + channelid + '\'' +
                ", premiumrate=" + premiumrate +
                ", mobilerate=" + mobilerate +
                ", insertdate=" + insertdate +
                ", homerate=" + homerate +
                ", searchrate=" + searchrate +
                ", suggestrate=" + suggestrate +
                ", directrate=" + directrate +
                ", enabled=" + enabled +
                ", viewpercent=" + viewpercent +
                ", maxthreads=" + maxthreads +
                ", title='" + title + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public Integer getPremiumrate() {
        return premiumrate;
    }

    public void setPremiumrate(Integer premiumrate) {
        this.premiumrate = premiumrate;
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

    public Integer getViewpercent() {
        return viewpercent;
    }

    public void setViewpercent(Integer viewpercent) {
        this.viewpercent = viewpercent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getMaxthreads() {
        return maxthreads;
    }

    public void setMaxthreads(Integer maxthreads) {
        this.maxthreads = maxthreads;
    }
}
