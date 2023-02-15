package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

public class ServiceOption {
    @Id
    private Integer service;
    private Integer direct;
    private Integer search;
    private Integer suggest;
    private Integer dtn;
    private Integer mintime;
    private Integer maxtime;

}
