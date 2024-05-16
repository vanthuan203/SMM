package com.nts.awspremium.model;

public interface OrderViewHistory {
    Long getOrderId();
    Integer getCancel();
    Long getEnddate();
    Long getInsertdate();
    String getNote();
    Float getPrice();
    Integer getService();
    String getUser();
    String getVideoid();
    Integer getViewtotal();
    Integer getVieworder();
    Integer getViewstart();
    Integer getViewend();
    Long getTimecheckbh();
    Long getTimestart();
    String getGeo();
}
