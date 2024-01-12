package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "setting")
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer maxorder;
    private Integer maxorderbuffhvn;
    private Integer maxorderbuffhus;
    private Float maxrunningam;
    private Float maxrunningpm;
    private Integer mintimebuff;
    private Integer maxthread;
    private Integer pricerate;
    private Integer bonus;
    private Integer maxordervn;
    private Integer maxorderus;
    private Integer levelthread;
    private Float leveluser;

    private Integer cmtcountuser;
    private Integer redirect;
    private Integer redirectvn;
    private Integer redirectus;
    private Integer threadmin;
    private Integer randview;

    public Setting(Long id, Integer maxorder, Integer maxorderbuffhvn, Integer maxorderbuffhus, Float maxrunningam, Float maxrunningpm, Integer mintimebuff, Integer maxthread, Integer pricerate, Integer bonus, Integer maxordervn, Integer maxorderus, Integer levelthread, Float leveluser, Integer cmtcountuser, Integer redirect, Integer redirectvn, Integer redirectus, Integer threadmin, Integer randview) {
        this.id = id;
        this.maxorder = maxorder;
        this.maxorderbuffhvn = maxorderbuffhvn;
        this.maxorderbuffhus = maxorderbuffhus;
        this.maxrunningam = maxrunningam;
        this.maxrunningpm = maxrunningpm;
        this.mintimebuff = mintimebuff;
        this.maxthread = maxthread;
        this.pricerate = pricerate;
        this.bonus = bonus;
        this.maxordervn = maxordervn;
        this.maxorderus = maxorderus;
        this.levelthread = levelthread;
        this.leveluser = leveluser;
        this.cmtcountuser = cmtcountuser;
        this.redirect = redirect;
        this.redirectvn = redirectvn;
        this.redirectus = redirectus;
        this.threadmin = threadmin;
        this.randview = randview;
    }

    public Setting() {
    }

    public Float getMaxrunningam() {
        return maxrunningam;
    }

    public void setMaxrunningam(Float maxrunningam) {
        this.maxrunningam = maxrunningam;
    }

    public Float getMaxrunningpm() {
        return maxrunningpm;
    }

    public void setMaxrunningpm(Float maxrunningpm) {
        this.maxrunningpm = maxrunningpm;
    }

    public Integer getLevelthread() {
        return levelthread;
    }

    public void setLevelthread(Integer levelthread) {
        this.levelthread = levelthread;
    }

    public Integer getMaxordervn() {
        return maxordervn;
    }

    public void setMaxordervn(Integer maxordervn) {
        this.maxordervn = maxordervn;
    }

    public Integer getMaxorderus() {
        return maxorderus;
    }

    public void setMaxorderus(Integer maxorderus) {
        this.maxorderus = maxorderus;
    }

    public Integer getBonus() {
        return bonus;
    }

    public void setBonus(Integer bonus) {
        this.bonus = bonus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMaxorder() {
        return maxorder;
    }

    public void setMaxorder(Integer maxorder) {
        this.maxorder = maxorder;
    }

    public Integer getMintimebuff() {
        return mintimebuff;
    }

    public void setMintimebuff(Integer mintimebuff) {
        this.mintimebuff = mintimebuff;
    }

    public Integer getMaxthread() {
        return maxthread;
    }

    public void setMaxthread(Integer maxthread) {
        this.maxthread = maxthread;
    }

    public Integer getPricerate() {
        return pricerate;
    }

    public void setPricerate(Integer pricerate) {
        this.pricerate = pricerate;
    }

    public Float getLeveluser() {
        return leveluser;
    }

    public void setLeveluser(Float leveluser) {
        this.leveluser = leveluser;
    }

    public Integer getCmtcountuser() {
        return cmtcountuser;
    }

    public Integer getRedirect() {
        return redirect;
    }

    public void setRedirect(Integer redirect) {
        this.redirect = redirect;
    }

    public Integer getThreadmin() {
        return threadmin;
    }

    public void setThreadmin(Integer threadmin) {
        this.threadmin = threadmin;
    }

    public void setCmtcountuser(Integer cmtcountuser) {
        this.cmtcountuser = cmtcountuser;
    }

    public Integer getMaxorderbuffhvn() {
        return maxorderbuffhvn;
    }

    public void setMaxorderbuffhvn(Integer maxorderbuffhvn) {
        this.maxorderbuffhvn = maxorderbuffhvn;
    }

    public Integer getMaxorderbuffhus() {
        return maxorderbuffhus;
    }

    public void setMaxorderbuffhus(Integer maxorderbuffhus) {
        this.maxorderbuffhus = maxorderbuffhus;
    }

    public Integer getRedirectvn() {
        return redirectvn;
    }

    public void setRedirectvn(Integer redirectvn) {
        this.redirectvn = redirectvn;
    }

    public Integer getRedirectus() {
        return redirectus;
    }

    public void setRedirectus(Integer redirectus) {
        this.redirectus = redirectus;
    }

    public Integer getRandview() {
        return randview;
    }

    public void setRandview(Integer randview) {
        this.randview = randview;
    }
}
