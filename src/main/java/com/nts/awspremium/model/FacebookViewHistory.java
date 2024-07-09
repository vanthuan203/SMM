package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "facebook_view_history")
public class FacebookViewHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id",referencedColumnName = "account_id",updatable = true,insertable = true)
    private Account account;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String list_id;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
}