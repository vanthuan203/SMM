package com.nts.awspremium.model;

import org.json.simple.JSONObject;

import javax.persistence.*;

@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String role;
    private String token;
    private Long balance;
    private Integer discount;
    private Long maxorder;
    private Integer vip;
    private String note;
    public Admin() {
    }

    public Admin(Long id, String username, String password, String role, String token, Long balance, Integer discount, Long maxorder, Integer vip, String note) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.token = token;
        this.balance = balance;
        this.discount = discount;
        this.maxorder = maxorder;
        this.vip = vip;
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getVip() {
        return vip;
    }

    public void setVip(Integer vip) {
        this.vip = vip;
    }

    public Long getMaxorder() {
        return maxorder;
    }

    public void setMaxorder(Long maxorder) {
        this.maxorder = maxorder;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", token='" + token + '\'' +
                '}';
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
