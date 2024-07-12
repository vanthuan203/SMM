package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "device")
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String device_id;
    @Column(columnDefinition = "bigint default 0")
    private Long add_time;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "integer default 0")
    private Integer num_profile;
    @Column(columnDefinition = "integer default 0")
    private Integer num_account;
    @Column(columnDefinition = "integer default 1")
    private Integer state;
}
