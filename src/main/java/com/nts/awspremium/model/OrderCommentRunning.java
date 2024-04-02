package com.nts.awspremium.model;

public interface OrderCommentRunning {
    Long getOrderId();
    String getVideoId();
    String getVideoTitle();
    Integer getTotal();
    Integer getMaxthreads();
    Integer getCommentStart();
    Long getInsertDate();
    String getNote();
    Long getDuration();
    String getUser();
    Integer getCommentOrder();
    Integer getCommentTotal();
    Integer getComment24h();
    Float getPrice();
    Integer getService();
    String getGeo();
    Long getTimeUpdate();
}
