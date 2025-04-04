package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "data_follower_tiktok")
@Setter
@Getter
public class DataFollowerTiktok {
    @Id
    private String video_id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",referencedColumnName = "order_id",updatable = true,insertable = true)
    private OrderRunning orderRunning;
    @Column(columnDefinition = "varchar(1555) default ''")
    private String video_title;
    @Column(columnDefinition = "varchar(555) default ''")
    private String tiktok_id;
    @Column(columnDefinition = "bigint default 0")
    private Long duration;
    @Column(columnDefinition = "integer default 0")
    private Integer state;
    @Column(columnDefinition = "bigint default 0")
    private Long add_time;
    public DataFollowerTiktok() {
    }
}
