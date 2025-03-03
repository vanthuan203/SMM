package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "platform")
public class Platform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String platform;
    @Column(columnDefinition = "integer default 0")
    private Integer priority;
    @Column(columnDefinition = "integer default 0")
    private Integer activity;
    @Column(columnDefinition = "integer default 0")
    private Integer state;
    @Column(columnDefinition = "integer default 0")
    private Integer connection_account=0;
    @Column(columnDefinition = "integer default 0")
    private Integer register_account=0;
    @Column(columnDefinition = "integer default 0")
    private Integer login_account=0;
    @Column(columnDefinition = "integer default 0")
    private Integer register_time=0;
    @Column(columnDefinition = "integer default 0")
    private Integer login_time=0;
    @Column(columnDefinition = "integer default 0")
    private Integer changer_time=0;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "bigint default 0")
    private Long version_app;
    @Column(columnDefinition = "bigint default 0")
    private Long change_info;
    @Column(columnDefinition = "varchar(255) default ''")
    private String dependent;
    @Column(columnDefinition = "varchar(255) default ''")
    private String mode="dev";
    public Platform() {
    }
}
