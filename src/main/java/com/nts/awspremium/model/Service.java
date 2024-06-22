package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "service")
public class Service implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Integer service_id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String service_name;
    @Column(columnDefinition = "varchar(255) default ''")
    private String service_type;
    @Column(columnDefinition = "varchar(255) default ''")
    private String service_category;
    @Column(columnDefinition = "float default 0")
    private Float service_rate;
    @Column(columnDefinition = "integer default 0")
    private Integer min_quantity;
    @Column(columnDefinition = "integer default 0")
    private Integer max_quantity;
    @Column(columnDefinition = "integer default 0")
    private Integer thread;
    @Column(columnDefinition = "varchar(255) default ''")
    private String note;
    @Column(columnDefinition = "varchar(255) default ''")
    private String geo;
    @Column(columnDefinition = "integer default 0")
    private Integer enabled;
    @Column(columnDefinition = "integer default 0")
    private Integer max_order;
    @Column(columnDefinition = "integer default 0")
    private Integer youtube_search;
    @Column(columnDefinition = "integer default 0")
    private Integer youtube_suggest;
    @Column(columnDefinition = "integer default 0")
    private Integer youtube_dtn;
    @Column(columnDefinition = "integer default 0")
    private Integer youtube_direct;
    @Column(columnDefinition = "integer default 0")
    private Integer youtube_embed;
    @Column(columnDefinition = "integer default 0")
    private Integer youtube_external;
    @Column(columnDefinition = "integer default 0")
    private Integer youtube_playlists;
    @Column(columnDefinition = "integer default 0")
    private Integer min_time;
    @Column(columnDefinition = "integer default 0")
    private Integer max_time;
    @Column(columnDefinition = "integer default 0")
    private Integer refund;
    @Column(columnDefinition = "integer default 0")
    private Integer refund_time;
    @Column(columnDefinition = "integer default 0")
    private Integer check_time;
    @Column(columnDefinition = "integer default 0")
    private Integer check_end_time;
    @Column(columnDefinition = "integer default 0")
    private Integer expired=0;
    @Column(columnDefinition = "integer default 0")
    private Integer youtube_niche;
    @Column(columnDefinition = "varchar(255) default ''")
    private String youtube_key_niche;
    @Column(columnDefinition = "integer default 0")
    private Integer website_click_web;
    @Column(columnDefinition = "integer default 0")
    private Integer website_click_ads;
    @Column(columnDefinition = "varchar(255) default ''")
    private String platform;
    @Column(columnDefinition = "varchar(255) default ''")
    private String task;
    @Column(columnDefinition = "integer default 0")
    private Integer youtube_reply;
    @Column(columnDefinition = "integer default 0")
    private Float bonus ;
    @Column(columnDefinition = "integer default 0")
    private Integer check_done ;
    @Column(columnDefinition = "integer default 0")
    private Integer check_count ;
    @Column(columnDefinition = "varchar(255) default 'all'")
    private String device_type;
}
