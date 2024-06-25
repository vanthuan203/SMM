package com.nts.awspremium.model;

public interface OrderRunningShow {
    Long getOrder_id();
    String getOrder_key();
    Integer getTotal_thread();
    Integer getThread();
    Long getInsert_time();
    Long getStart_time();
    String getNote();
    String getUsername();
    Long getUpdate_time();
    Integer getTotal();
    Float getCharge();
    Integer getService_id();
    Integer getQuantity();
    Integer getStart_count();
    Integer getCheck_count();
    Integer getCurrent_count();
    String getPlatform();
    String getTask();
    Integer getBonus();

}
