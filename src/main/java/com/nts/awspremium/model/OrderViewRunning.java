package com.nts.awspremium.model;

public interface OrderViewRunning {
    Long getOrderId();
    String getVideoId();
    String getVideoTitle();
    Integer getTotal();
    Integer getMaxthreads();
    Integer getViewStart();
    Long getInsertDate();
    Long getTimeStart();
    String getNote();
    Long getDuration();
    String getUser();
    Integer getViewOrder();
    Integer getViewTotal();
    Integer getView24h();
    Float getPrice();
    Integer getService();
    Long getTimeUpdate();
}
