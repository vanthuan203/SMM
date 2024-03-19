package com.nts.awspremium.model;

import javax.persistence.*;

@Table(name = "history_follower_tiktok_sum")
@Entity
public class HistoryFollowerTikTokSum {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private Long orderid;
    private Long time;
    public HistoryFollowerTikTokSum() {
    }

    public HistoryFollowerTikTokSum(Long id, String username, Long orderid, Long time) {
        this.id = id;
        this.username = username;
        this.orderid = orderid;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
