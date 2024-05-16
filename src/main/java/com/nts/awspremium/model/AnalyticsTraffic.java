package com.nts.awspremium.model;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnalyticsTraffic {
    private Long orderid;
    private String token;

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
