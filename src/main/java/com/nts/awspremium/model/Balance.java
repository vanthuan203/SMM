package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "blance")
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String user;
    private Long balance;
    private Long totalblance;
    private Long time;
    private String note;
    public Balance() {
    }

    public Balance(Long id, String user, Long balance, Long totalblance, Long time, String note) {
        this.id = id;
        this.user = user;
        this.balance = balance;
        this.totalblance = totalblance;
        this.time = time;
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getTotalblance() {
        return totalblance;
    }

    public void setTotalblance(Long totalblance) {
        this.totalblance = totalblance;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
