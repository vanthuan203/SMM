package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "datacomment")
public class DataComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderid;
    private String comment;
    private String username;
    private Integer running;
    private Long timeget;
    private String vps;

    public DataComment() {
    }

    public DataComment(Long id, Long orderid, String comment, String username, Integer running, Long timeget, String vps) {
        this.id = id;
        this.orderid = orderid;
        this.comment = comment;
        this.username = username;
        this.running = running;
        this.timeget = timeget;
        this.vps = vps;
    }

    public String getVps() {
        return vps;
    }

    public void setVps(String vps) {
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
}
