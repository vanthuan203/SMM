package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "channel_tiktok_history")
public class ChannelTikTokHistory {
    @Id
    private Long orderid;
    private String tiktok_id;
    private Float price;
    private Integer follower_order;
    private Integer follower_start;
    private Integer follower_total;
    private Integer follower_end;
    private Long insert_date;
    private Long end_date;
    private Long time_check=0L;
    private Long time_update=0L;
    private Integer cancel;

    private Integer service;
    private Integer max_threads;
    private String note;
    private String user;
    private Integer refund=0;
    private Long time_check_refill=0L;
    private Long time_start=0L;
    public ChannelTikTokHistory() {
    }

    public ChannelTikTokHistory(Long orderid, String tiktok_id, Float price, Integer follower_order, Integer follower_start, Integer follower_total, Integer follower_end, Long insert_date, Long end_date, Long time_check, Long time_update, Integer cancel, Integer service, Integer max_threads, String note, String user, Integer refund, Long time_check_refill, Long time_start) {
        this.orderid = orderid;
        this.tiktok_id = tiktok_id;
        this.price = price;
        this.follower_order = follower_order;
        this.follower_start = follower_start;
        this.follower_total = follower_total;
        this.follower_end = follower_end;
        this.insert_date = insert_date;
        this.end_date = end_date;
        this.time_check = time_check;
        this.time_update = time_update;
        this.cancel = cancel;
        this.service = service;
        this.max_threads = max_threads;
        this.note = note;
        this.user = user;
        this.refund = refund;
        this.time_check_refill = time_check_refill;
        this.time_start = time_start;
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

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getFollower_order() {
        return follower_order;
    }

    public void setFollower_order(Integer follower_order) {
        this.follower_order = follower_order;
    }

    public Integer getFollower_start() {
        return follower_start;
    }

    public void setFollower_start(Integer follower_start) {
        this.follower_start = follower_start;
    }

    public Integer getFollower_total() {
        return follower_total;
    }

    public void setFollower_total(Integer follower_total) {
        this.follower_total = follower_total;
    }

    public Long getInsert_date() {
        return insert_date;
    }

    public void setInsert_date(Long insert_date) {
        this.insert_date = insert_date;
    }

    public Long getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Long end_date) {
        this.end_date = end_date;
    }

    public Long getTime_check() {
        return time_check;
    }

    public void setTime_check(Long time_check) {
        this.time_check = time_check;
    }

    public Long getTime_update() {
        return time_update;
    }

    public void setTime_update(Long time_update) {
        this.time_update = time_update;
    }

    public Integer getCancel() {
        return cancel;
    }

    public void setCancel(Integer cancel) {
        this.cancel = cancel;
    }

    public Integer getService() {
        return service;
    }

    public void setService(Integer service) {
        this.service = service;
    }

    public Integer getMax_threads() {
        return max_threads;
    }

    public void setMax_threads(Integer max_threads) {
        this.max_threads = max_threads;
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

    public Integer getRefund() {
        return refund;
    }

    public void setRefund(Integer refund) {
        this.refund = refund;
    }

    public Long getTime_check_refill() {
        return time_check_refill;
    }

    public void setTime_check_refill(Long time_check_refill) {
        this.time_check_refill = time_check_refill;
    }

    public Long getTime_start() {
        return time_start;
    }

    public Integer getFollower_end() {
        return follower_end;
    }

    public void setFollower_end(Integer follower_end) {
        this.follower_end = follower_end;
    }

    public void setTime_start(Long time_start) {
        this.time_start = time_start;
    }
}
