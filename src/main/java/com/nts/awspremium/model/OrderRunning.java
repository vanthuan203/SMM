package com.nts.awspremium.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

public interface OrderRunning {
    String getTitle();
    String getChannelId();
    Integer getMaxthreads();
    Integer getTotal();

    Integer getViewpercent();

    Long getInsertdate();

    Integer getEnabled();


}
