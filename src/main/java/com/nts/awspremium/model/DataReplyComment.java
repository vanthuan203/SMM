package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "data_reply_comment")
public class DataReplyComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderid;
    private Long comment_id;
    private String reply;
    private String link;
    private String username;
    private Integer running;
    private Long timeget;
    private String vps;

    public DataReplyComment() {
    }

    public DataReplyComment(Long id, Long orderid, Long comment_id, String reply, String link, String username, Integer running, Long timeget, String vps) {
        this.id = id;
        this.orderid = orderid;
        this.comment_id = comment_id;
        this.reply = reply;
        this.link = link;
        this.username = username;
        this.running = running;
        this.timeget = timeget;
        this.vps = vps;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public Long getComment_id() {
        return comment_id;
    }

    public void setComment_id(Long comment_id) {
        this.comment_id = comment_id;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public Long getTimeget() {
        return timeget;
    }

    public void setTimeget(Long timeget) {
        this.timeget = timeget;
    }

    public String getVps() {
        return vps;
    }

    public void setVps(String vps) {
        this.vps = vps;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
