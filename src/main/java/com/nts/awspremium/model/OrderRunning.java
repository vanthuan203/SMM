package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "order_running")
@Getter
@Setter
public class OrderRunning implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long order_id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String order_key;
    @Column(columnDefinition = "varchar(500) default ''")
    private String order_link;
    @Column(columnDefinition = "varchar(255) default ''")
    private String video_title;
    @Column(columnDefinition = "varchar(255) default ''")
    private String channel_id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String channel_title;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String comment_list;
    @Column(columnDefinition = "TEXT")
    private String keyword_list;
    @Column(columnDefinition = "TEXT")
    private String video_list;
    @Column(columnDefinition = "integer default 0")
    private Integer start_count;
    @Column(columnDefinition = "bigint default 0")
    private Long insert_time;
    @Column(columnDefinition = "bigint default 0")
    private Long start_time;
    @Column(columnDefinition = "bigint default 0")
    private Long check_count_time;
    @Column(columnDefinition = "bigint default 0")
    private Long start_count_time;
    @Column(columnDefinition = "integer default 0")
    private Integer thread;
    @Column(columnDefinition = "integer default 0")
    private Integer check_count;
    @Column(columnDefinition = "integer default 0")
    private Integer thread_set;
    @Column(columnDefinition = "bigint default 0")
    private Long duration;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id",referencedColumnName = "service_id",nullable = false,updatable = true,insertable = true)
    private Service service;
    @Column(columnDefinition = "varchar(255) default ''")
    private String note;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username",referencedColumnName = "username",nullable = false,updatable = true,insertable = true)
    private User user;
    @Column(columnDefinition = "integer default 0")
    private Integer quantity;
    @Column(columnDefinition = "integer default 0")
    private Integer current_count;
    @Column(columnDefinition = "integer default 0")
    private Integer total;
    @Column(columnDefinition = "integer default 0")
    private Integer time_total;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "bigint default 0")
    private Long update_current_time;
    @Column(columnDefinition = "float default 0")
    private Float charge;
    @Column(columnDefinition = "integer default 1")
    private Integer valid;
    @Column(columnDefinition = "integer default 0")
    private Integer priority;
    @Column(columnDefinition = "integer default 0")
    private Integer speed_up;
    @Column(columnDefinition = "bigint default 0")
    private Long order_refill;
    @Version
    private Long version=0L;
    public OrderRunning() {
    }

}
