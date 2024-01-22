package com.nts.awspremium.model;

public interface OrderTrafficRunning {
    Long getOrderId();
    String getLink();
    Integer getTotal();
    Integer getMaxthreads();
    Long getInsertDate();
    Long getTimeStart();
    String getNote();
    String getUser();
    Integer getTrafficOrder();
    Integer getTrafficTotal();
    Integer getTraffic24h();
    Float getPrice();
    Integer getService();
    Integer getPriority();
    String getKeywords();
    Long getTimeUpdate();
}
