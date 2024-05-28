package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Table(name = "history_sum")
@Entity
@Setter
@Getter
public class HistorySum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String account_id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",referencedColumnName = "order_id",updatable = true,insertable = true)
    private OrderRunning orderRunning;
    @Column(columnDefinition = "bigint default 0")
    private Long add_time;
}
