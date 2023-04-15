package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "balance")
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String user;
    private Float balance;
    private Float totalblance;
    private Long time;
    private String note;
    private String currency;
    private Integer service;
    public Balance() {
    }

    public Balance(Long id, String user, Float balance, Float totalblance, Long time, String note, String currency, Integer service) {
        this.id = id;
        this.user = user;
        this.balance = balance;
        this.totalblance = totalblance;
        this.time = time;
        this.note = note;
        this.currency = currency;
        this.service = service;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    public Float getTotalblance() {
        return totalblance;
    }

    public void setTotalblance(Float totalblance) {
        this.totalblance = totalblance;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getService() {
        return service;
    }

    public void setService(Integer service) {
        this.service = service;
    }
}
