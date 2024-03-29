package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "proxysetting")
public class ProxySetting {
    @Id
    private Long id ;
    private String option_proxy;
    private Integer total_port;
    private Integer total_sock_port;
    private String username;
    private String password;
    private String cron;
    private Integer create_version;
    private String create_url;
    private String update_ip_version;
    private String update_ip_url;
    private String crontab_version;
    private String crontab_url;
    private Long timeupdate;
    public ProxySetting() {
    }

    public String getCrontab_version() {
        return crontab_version;
    }

    public void setCrontab_version(String crontab_version) {
        this.crontab_version = crontab_version;
    }

    public String getCrontab_url() {
        return crontab_url;
    }

    public void setCrontab_url(String crontab_url) {
        this.crontab_url = crontab_url;
    }

    public String getUpdate_ip_version() {
        return update_ip_version;
    }

    public void setUpdate_ip_version(String update_ip_version) {
        this.update_ip_version = update_ip_version;
    }

    public String getUpdate_ip_url() {
        return update_ip_url;
    }

    public void setUpdate_ip_url(String update_ip_url) {
        this.update_ip_url = update_ip_url;
    }

    public Integer getCreate_version() {
        return create_version;
    }

    public void setCreate_version(Integer create_version) {
        this.create_version = create_version;
    }

    public String getCreate_url() {
        return create_url;
    }

    public void setCreate_url(String create_url) {
        this.create_url = create_url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOption_proxy() {
        return option_proxy;
    }

    public void setOption_proxy(String option_proxy) {
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
