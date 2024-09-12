package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "varchar(255) default 'ROLE_USER'")
    private String role;
    @Column(nullable = false)
    private String token;
    @Column(columnDefinition = "varchar(255) default ''")
    private String note;

    @Column(columnDefinition = "integer default 100")
    private Integer rate;

    @Column(columnDefinition = "integer default 1000")
    private Integer max_order;

    @Column(columnDefinition = "integer default 0")
    private Integer discount;
    @Column(columnDefinition = "integer default 0")
    private Integer vip;
    @Column(columnDefinition = "bigint default 0")
    private Long time_add;
    @Column(columnDefinition = "float default 0")
    private Float balance;


}
