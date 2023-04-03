package com.nts.awspremium.model;

import javax.persistence.*;

@Table(name = "historycommentsum")
@Entity
public class HistoryCommentSum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Long orderid;
    private Long commentid;
    private Long time;
    public HistoryCommentSum() {
    }

    public HistoryCommentSum(Long id, String username, Long orderid, Long commentid, Long time) {
        this.id = id;
        this.username = username;
        this.orderid = orderid;
        this.commentid = commentid;
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

    public Long getCommentid() {
        return commentid;
    }

    public void setCommentid(Long commentid) {
        this.commentid = commentid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
