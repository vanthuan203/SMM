package com.nts.awspremium.model;

public interface OrderCommentHistory {
    Long getOrderId();
    Integer getCancel();
    Long getEnddate();
    Long getInsertdate();
    String getNote();
    Float getPrice();
    Integer getService();
    String getUser();
    String getVideoid();
    Integer getCommenttotal();
    Integer getCommentorder();
    Integer getCommentstart();
    Integer getCommentend();
    String getGeo();
}
