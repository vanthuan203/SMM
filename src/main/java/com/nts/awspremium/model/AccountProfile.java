package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "account_profile")
public class AccountProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String account_id;
    @Column(nullable = false)
    private String password;
    @Column(columnDefinition = "varchar(255) default ''")
    private String name="";

    @Column(columnDefinition = "integer default 0")
    private Integer avatar=0;

    @Column(columnDefinition = "varchar(255) default ''")
    private String recover;

    @Column(columnDefinition = "integer default 1")
    private Integer live;

    @Column(columnDefinition = "integer default 0")
    private Integer running;

    @Column(columnDefinition = "integer default 0")
    private Integer changed;

    @Column(columnDefinition = "bigint default 0")
    private Long add_time;

    @Column(columnDefinition = "bigint default 0")
    private Long update_time;

    @Column(columnDefinition = "bigint default 0")
    private Long login_time=0L;

    @Column(columnDefinition = "varchar(255) default ''")
    private String auth_2fa="";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id",referencedColumnName = "profile_id",updatable = true,insertable = true)
    private ProfileTask profileTask;

    @Column(columnDefinition = "varchar(255) default ''")
    private String platform="";


}
