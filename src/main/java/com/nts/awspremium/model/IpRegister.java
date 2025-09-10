package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "ip_register")
public class IpRegister {
    @Id
    private String id;
    @Column(columnDefinition = "TINYINT default 0")
    private Boolean success=false;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "varchar(255) default ''")
    private String platform;
}