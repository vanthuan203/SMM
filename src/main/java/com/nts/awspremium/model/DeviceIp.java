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
@Table(name = "device_ip")
public class DeviceIp implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String device_id;
    @Column(columnDefinition = "bigint default 0")
    private Long ip_changer_time;
    @Column(columnDefinition = "TEXT default ''")
    private String ip_list;
}
