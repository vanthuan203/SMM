package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataRequest {
    private String key;
    private String link;
    private int quantity;
    private String action;
    private int service=-1;
    private Long order=-1L;
    private Long order_refill=-1L;
    private String orders="";
    private String list="";
    private int thread=0;
    private String note="";
    private String search="";
    private String suggest="";
    private String comments="";


}
