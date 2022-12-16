package com.nts.awspremium.model;

public interface OrderBuffhRunning {
    String getVideoId();
    String getVideoTitle();
    Integer getTotal();
    Integer getMaxthreads();
    Integer getTimeBuff();
    Integer getViewStart();
    Long getInsertDate();
    Integer getEnabled();
    String getNote();
    Long getDuration();
    Integer getOptionBuff();
    Integer getMobileRate();
    Integer getSearchRate();
    Integer getSuggestRate();
    Integer getDirectRate();
    Integer getHomeRate();
    Integer getLikeRate();
    Integer getCommentRate();
}
