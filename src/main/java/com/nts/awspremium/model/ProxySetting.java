package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "proxysetting")
public class ProxySetting {
    @Id
    private Long id ;
    private Integer option_proxy;
    private Integer total_port;
    private Integer total_sock_port;
    private String username;
    private String password;
    private String cron;
    private Long timeupdate;
    public ProxySetting() {
    }

    public ProxySetting(Long id, Integer option_proxy, Integer total_port, Integer total_sock_port, String username, String password, String cron, Long timeupdate) {
        this.id = id;
        this.option_proxy = option_proxy;
        this.total_port = total_port;
        this.total_sock_port = total_sock_port;
        this.username = username;
        this.password = password;
        this.cron = cron;
        this.timeupdate = timeupdate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOption_proxy() {
        return option_proxy;
    }

    public void setOption_proxy(Integer option_proxy) {
        this.option_proxy = option_proxy;
    }

    public Integer getTotal_port() {
        return total_port;
    }

    public void setTotal_port(Integer total_port) {
        this.total_port = total_port;
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

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Long getTimeupdate() {
        return timeupdate;
    }

    public void setTimeupdate(Long timeupdate) {
        this.timeupdate = timeupdate;
    }

    public Integer getTotal_sock_port() {
        return total_sock_port;
    }

    public void setTotal_sock_port(Integer total_sock_port) {
        this.total_sock_port = total_sock_port;
    }
}
