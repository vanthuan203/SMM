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
@Table(name = "ip_task_24h")
public class IpTask24h {
    @Id
    private String id;
    @Column(columnDefinition = "TINYINT default 1")
    private Boolean success=true;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
}