package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "setting")
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer maxorder;
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

    private Integer threadmin;

    public Setting() {
    }

    public Setting(Long id, Integer maxorder, Integer mintimebuff, Integer maxthread, Integer pricerate, Integer bonus, Integer maxordervn, Integer maxorderus, Integer levelthread, Float leveluser, Integer cmtcountuser, Integer redirect, Integer threadmin) {
        this.id = id;
        this.maxorder = maxorder;
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
        this.threadmin = threadmin;
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
}
