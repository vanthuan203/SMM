package com.nts.awspremium.model;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;
@Getter
@Setter
@Entity
@Table(name = "account")
public class Account {
    @Id
    private String account_id;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "varchar(255) default ''")
    private String recover_mail;

    @Column(columnDefinition = "integer default 1")
    private Integer live;

    @Column(columnDefinition = "integer default 0")
    private Integer running;

    @Column(columnDefinition = "bigint default 0")
    private Long add_time;

    @Column(columnDefinition = "bigint default 0")
    private Long get_time;

    @Column(columnDefinition = "varchar(255) default ''")
    private String auth_2fa="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String profile_id="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String device_id="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String computer_id="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String platform="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String note="";
    @Column(columnDefinition = "varchar(255) default ''")
    private String code="";

}
