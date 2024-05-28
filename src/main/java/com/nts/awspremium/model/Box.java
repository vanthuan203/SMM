package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "box")
public class Box {
    @Id
    private String box_id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "computer_id",referencedColumnName = "computer_id",updatable = true,insertable = true)
    private Computer computer;
    @Column(columnDefinition = "bigint default 0")
    private Long add_time;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "integer default 1")
    private Integer state;
}
