package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "order_comment")
@Setter
@Getter
public class OrderComment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",referencedColumnName = "order_id",updatable = true,insertable = true)
    private OrderRunning orderRunning;
    @Column(columnDefinition = "varchar(255) default ''")
    private String ai_uuid;
    @Column(columnDefinition = "integer default 0")
    private Integer count_render;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    public OrderComment() {
    }
}
