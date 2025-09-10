package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "history_register")
public class HistoryRegister implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id",referencedColumnName = "profile_id",updatable = true,insertable = true)
    private ProfileTask profileTask;
    @Column(columnDefinition = "varchar(255) default ''")
    private String platform;
    @Column(columnDefinition = "varchar(255) default ''")
    private String ip_address="";
    @Column(columnDefinition = "integer default 0")
    private Integer state=0;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
}