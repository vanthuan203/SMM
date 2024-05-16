package com.nts.awspremium.model;

public interface OrderFollowerTikTokRunning {
    Long getOrderId();
    String getTiktok_id();
    Integer getTotal();
    Long getInsert_date();
    Long getTime_start();
    Integer getMax_threads();
    Integer getService();
    String getNote();
    String getUser();
    Integer getFollower_order();
    Integer getFollower_total();
    Integer getFollower_start();
    Float getPrice();
    Integer getValid();

    Integer getPriority();
}
