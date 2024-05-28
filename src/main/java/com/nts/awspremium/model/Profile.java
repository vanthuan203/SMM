package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "profile")
public class Profile {
    @Id
    private String profile_id;
    @Column(columnDefinition = " varchar(255) default ''")
    private String barcode;
    @Column(columnDefinition = " varchar(255) default ''")
    private String imei;
    @Column(columnDefinition = " varchar(255) default ''")
    private String bt_addr;
    @Column(columnDefinition = " varchar(255) default ''")
    private String wifi_addr;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id",referencedColumnName = "device_id",updatable = true,insertable = true)
    private Device device;
    @Column(columnDefinition = " varchar(255) default ''")
    private Long add_time;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "integer default -1")
    private Integer state;
    @Column(columnDefinition = "integer default 0")
    private Integer num_account;
}
