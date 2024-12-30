package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "mode")
public class Mode {
    @Id
    private String mode;
    @Column(columnDefinition = "integer default 0")
    private Integer time_profile;
    @Column(columnDefinition = "integer default 0")
    private Integer time_enable_profile;
    @Column(columnDefinition = "integer default 0")
    private Integer max_profile;
    @Column(columnDefinition = "integer default 0")
    private Integer max_account;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    public Mode() {
    }
}
