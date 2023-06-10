package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "recover")
public class Recover {
    @Id
    private String username;
    private String password;
    private Long timeget;
    private Integer count;


    public Recover() {
    }

    public Recover(String username, String password, Long timeget, Integer count) {
        this.username = username;
        this.password = password;
        this.timeget = timeget;
        this.count = count;
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

    public Long getTimeget() {
        return timeget;
    }

    public void setTimeget(Long timeget) {
        this.timeget = timeget;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
