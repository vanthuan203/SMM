package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "channel_tiktok")
public class ChannelTiktok {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderid;
    private String tiktok_id;
    private Long insert_date;
    private Long time_start;
    private Integer max_threads;
    private Integer service;
    private String note;
    private String user;
    private Integer follower_order=0;
    private Integer follower_total=0;
    private Integer follower_start=0;
    private Long time_update=0L;
    private Float price;
    private Integer valid=1;
    private Integer priority=0;
    private Integer speed_up=0;

    public ChannelTiktok() {
    }

    public ChannelTiktok(Long orderid, String tiktok_id, Long insert_date, Long time_start, Integer max_threads, Integer service, String note, String user, Integer follower_order, Integer follower_total, Integer follower_start, Long time_update, Float price, Integer valid, Integer priority, Integer speed_up) {
        this.orderid = orderid;
        this.tiktok_id = tiktok_id;
        this.insert_date = insert_date;
        this.time_start = time_start;
        this.max_threads = max_threads;
        this.service = service;
        this.note = note;
        this.user = user;
        this.follower_order = follower_order;
        this.follower_total = follower_total;
        this.follower_start = follower_start;
        this.time_update = time_update;
        this.price = price;
        this.valid = valid;
        this.priority = priority;
        this.speed_up = speed_up;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public String getTiktok_id() {
        return tiktok_id;
    }

    public void setTiktok_id(String tiktok_id) {
        this.tiktok_id = tiktok_id;
    }

    public Long getInsert_date() {
        return insert_date;
    }

    public void setInsert_date(Long insert_date) {
        this.insert_date = insert_date;
    }

    public Long getTime_start() {
        return time_start;
    }

    public void setTime_start(Long time_start) {
        this.time_start = time_start;
    }

    public Integer getMax_threads() {
        return max_threads;
    }

    public void setMax_threads(Integer max_threads) {
        this.max_threads = max_threads;
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

    public Integer getFollower_order() {
        return follower_order;
    }

    public void setFollower_order(Integer follower_order) {
        this.follower_order = follower_order;
    }

    public Integer getFollower_total() {
        return follower_total;
    }

    public void setFollower_total(Integer follower_total) {
        this.follower_total = follower_total;
    }

    public Integer getFollower_start() {
        return follower_start;
    }

    public void setFollower_start(Integer follower_start) {
        this.follower_start = follower_start;
    }

    public Long getTime_update() {
        return time_update;
    }

    public void setTime_update(Long time_update) {
        this.time_update = time_update;
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

    public Integer getSpeed_up() {
        return speed_up;
    }

    public void setSpeed_up(Integer speed_up) {
        this.speed_up = speed_up;
    }
}
