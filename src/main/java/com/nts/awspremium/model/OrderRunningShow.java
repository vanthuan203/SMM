package com.nts.awspremium.model;

public interface OrderRunningShow {
    Long getOrder_id();
    String getOrder_key();
    String getOrder_link();
    Integer getTotal_thread();
    Integer getThread();
    Long getInsert_time();
    Long getStart_time();
    String getNote();
    String getUsername();
    Long getUpdate_time();
    Long getUpdate_current_time();
    Integer getTotal_limit_time();
    Integer getTotal();
    Float getCharge();
    Integer getService_id();
    Integer getQuantity();
    Integer getStart_count();
    Integer getCheck_count();
    Long getCheck_count_time();
    Integer getCurrent_count();
    String getPlatform();
    String getTask();
    String getMode();
    Integer getBonus();
    Integer getBonus_check();
    Integer getPriority();


}
