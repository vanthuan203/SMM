package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "accountsub")
public class AccountSub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String oldpassword;
    private String recover;
    private Integer live;
    private Long timeupdateinfo;
    private Long insertdate;
    private String vps;
    private Integer running;
    private Long timecheck;
    private  String note;
    public AccountSub() {
    }

    public AccountSub(Long id, String username, String password, String oldpassword, String recover, Integer live, Long timeupdateinfo, Long insertdate, String vps, Integer running, Long timecheck, String note) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.oldpassword = oldpassword;
        this.recover = recover;
        this.live = live;
        this.timeupdateinfo = timeupdateinfo;
        this.insertdate = insertdate;
        this.vps = vps;
        this.running = running;
        this.timecheck = timecheck;
        this.note = note;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOldpassword() {
        return oldpassword;
    }

    public void setOldpassword(String oldpassword) {
        this.oldpassword = oldpassword;
    }

    public String getRecover() {
        return recover;
    }

    public void setRecover(String recover) {
        this.recover = recover;
    }

    public Integer getLive() {
        return live;
    }

    public void setLive(Integer live) {
        this.live = live;
    }

    public Long getTimeupdateinfo() {
        return timeupdateinfo;
    }

    public void setTimeupdateinfo(Long timeupdateinfo) {
        this.timeupdateinfo = timeupdateinfo;
    }

    public Long getInsertdate() {
        return insertdate;
    }

    public void setInsertdate(Long insertdate) {
        this.insertdate = insertdate;
    }

    public String getVps() {
        return vps;
    }

    public void setVps(String vps) {
        this.vps = vps;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public Long getTimecheck() {
        return timecheck;
    }

    public void setTimecheck(Long timecheck) {
        this.timecheck = timecheck;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

