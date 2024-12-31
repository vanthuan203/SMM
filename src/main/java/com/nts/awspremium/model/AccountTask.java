package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "account_task")
@Setter
@Getter
public class AccountTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id",referencedColumnName = "account_id",updatable = true,insertable = true)
    private AccountProfile account;
    @Column(columnDefinition = "bigint default 0")
    private Long view_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long like_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long follower_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long subscriber_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long comment_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long repost_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long member_time=0L;
    @Column(columnDefinition = "bigint default 0")
    private Long task_time=0L;
    @Column(columnDefinition = "varchar(255) default ''")
    private String platform="";
    @Column(columnDefinition = "integer default 0")
    private Integer task_done_24h=0;
    @Column(columnDefinition = "integer default 0")
    private Integer task_success_24h=0;
    public AccountTask() {
    }
}
