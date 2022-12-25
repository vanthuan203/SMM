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

    public Setting() {
    }

    public Setting(Long id, Integer maxorder, Integer mintimebuff, Integer maxthread, Integer pricerate) {
        this.id = id;
        this.maxorder = maxorder;
        this.mintimebuff = mintimebuff;
        this.maxthread = maxthread;
        this.pricerate = pricerate;
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
}
