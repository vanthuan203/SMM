package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "account_clone")
public class AccountClone implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String account_id;
    @Column(nullable = false)
    private String id_clone;
    @Column(nullable = false)
    private String unique_clone;
    @Column(columnDefinition = "varchar(255) default ''")
    private String name="";

    @Column(columnDefinition = "integer default 0")
    private Integer avatar=0;

    @Column(columnDefinition = "varchar(5555) default ''")
    private String avatar_link;
    @Column(columnDefinition = "varchar(5555) default ''")
    private String video_list;

    @Column(columnDefinition = "bigint default 0")
    private Long add_time;

    @Column(columnDefinition = "bigint default 0")
    private Long update_time;

    @Column(columnDefinition = "varchar(255) default ''")
    private String platform="";


}
