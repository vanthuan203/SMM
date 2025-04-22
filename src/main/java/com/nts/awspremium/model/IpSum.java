package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "ip_sum")
public class IpSum implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String ip;
    @Column(columnDefinition = "bigint default 0")
    private Long add_time;
    @Column(columnDefinition = "varchar(555) default ''")
    private String info;
}
