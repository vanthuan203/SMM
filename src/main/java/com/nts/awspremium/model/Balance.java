package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
@Setter
@Getter
@Entity
@Table(name = "balance")
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "varchar(255) default 0")
    private String user;
    private Float balance;
    private Float total_blance;
    private Long add_time;
    @Column(columnDefinition = "varchar(255) default 0")
    private String note;
    private Integer service;
    @Column(columnDefinition = "integer default 0")
    private Integer noti=0;
    public Balance() {
    }

}
