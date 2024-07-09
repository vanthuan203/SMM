package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "data_comment")
@Setter
@Getter
public class DataComment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long comment_id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",referencedColumnName = "order_id",updatable = true,insertable = true)
    private OrderRunning orderRunning;
    @Column(columnDefinition = "TEXT")
    private String comment;
    @Column(columnDefinition = "varchar(255) default ''")
    private String account_id;
    @Column(columnDefinition = "integer default 0")
    private Integer running;
    @Column(columnDefinition = "bigint default 0")
    private Long get_time;
    @Column(columnDefinition = "varchar(255) default ''")
    private String device_id;
    public DataComment() {
    }
}
