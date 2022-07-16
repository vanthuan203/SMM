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

    public Admin() {
    }

    public Admin(String username, String password, String role, String token) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.token = token;
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

    public JSONObject getJsonObj() {
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("role", role);
        obj.put("enabled", 1);
        obj.put("balance", 0);
        obj.put("id", id);
        return obj;

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
