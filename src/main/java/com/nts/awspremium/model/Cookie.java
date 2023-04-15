package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "Cookie")
public class Cookie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String cookie;

    public Cookie() {
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

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public Cookie(String username, String cookie) {
        this.username = username;
        this.cookie = cookie;
    }
}
