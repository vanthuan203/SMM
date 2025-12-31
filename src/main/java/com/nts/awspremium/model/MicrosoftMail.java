package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "microsoft_mail")
public class MicrosoftMail implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(columnDefinition = "varchar(555) default ''")
    private String client_id="";
    @Column(columnDefinition = "varchar(5555) default ''")
    private String refresh_token1="";
    @Column(columnDefinition = "varchar(5555) default ''")
    private String refresh_token2="";
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
    @Column(columnDefinition = "bigint default 0")
    private Long update_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long die_time=0L;
    @Column(columnDefinition = "varchar(255) default ''")
    private String auth_2fa="";
    @Column(columnDefinition = "varchar(255) default ''")
    private String note="";
    @Column(columnDefinition = "varchar(255) default ''")
    private String code="";
}
