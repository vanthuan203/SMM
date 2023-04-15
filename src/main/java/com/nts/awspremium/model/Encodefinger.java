package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "Encodefinger")
public class Encodefinger {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private String encodefinger;

    public Encodefinger(String username, String encodefinger) {
        this.username = username;
        this.encodefinger = encodefinger;
    }

    public Encodefinger() {
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

    public String getEncodefinger() {
        return encodefinger;
    }

    public void setEncodefinger(String encodefinger) {
        this.encodefinger = encodefinger;
    }
}
