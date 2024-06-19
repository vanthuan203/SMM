package com.nts.awspremium.model;

public interface OrderHistoryShow {
    Long getOrder_id();
    String getOrder_key();

    Long getInsert_time();
    Long getStart_time();
    Long getEnd_time();
    String getNote();
    String getUsername();
    Long getUpdate_time();
    Integer getTotal();
    Integer getCancel();
    Float getCharge();
    Integer getService_id();
    Integer getQuantity();
    Integer getStart_count();
    Integer getCheck_count();
    Integer getCurrent_count();
    String getPlatform();
    String getTask();

}
