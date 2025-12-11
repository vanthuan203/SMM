package com.nts.awspremium.model;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "account")
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String account_id;
    @Column(nullable = false)
    private String password;
    @Column(columnDefinition = "varchar(255) default ''")
    private String uuid="";
    @Column(columnDefinition = "varchar(255) default ''")
    private String name="";

    @Column(columnDefinition = "integer default 0")
    private Integer avatar=0;

    @Column(columnDefinition = "varchar(255) default ''")
    private String recover_mail;

    @Column(columnDefinition = "varchar(255) default ''")
    private String recover_mail_password="";

    @Column(columnDefinition = "integer default 1")
    private Integer live;

    @Column(columnDefinition = "integer default 0")
    private Integer running;

    @Column(columnDefinition = "bigint default 0")
    private Long add_time;

    @Column(columnDefinition = "bigint default 0")
    private Long get_time;

    @Column(columnDefinition = "bigint default 0")
    private Long update_time=0L;

    @Column(columnDefinition = "bigint default 0")
    private Long changed_time=0L;

    @Column(columnDefinition = "bigint default 0")
    private Long die_time=0L;

    @Column(columnDefinition = "integer default 0")
    private Integer changed=0;

    @Column(columnDefinition = "varchar(255) default 'login'")
    private String mode;

    @Column(columnDefinition = "varchar(255) default ''")
    private String auth_2fa="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String profile_id="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String box_id="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String device_id="";

    @Column(columnDefinition = "varchar(255) default 'auto'")
    private String device_mode="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String computer_id="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String platform="";

    @Column(columnDefinition = "varchar(255) default ''")
    private String note="";
    @Column(columnDefinition = "varchar(255) default ''")
    private String dependent="";
    @Column(columnDefinition = "varchar(555) default ''")
    private String password_dependent="";
    @Column(columnDefinition = "varchar(255) default ''")
    private String die_dependent="";
    @Column(columnDefinition = "varchar(255) default ''")
    private String code="";
}
