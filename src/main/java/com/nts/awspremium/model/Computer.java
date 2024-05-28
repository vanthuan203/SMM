package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "computer")
public class Computer {
    @Id
    private String computer_id;
    @Column(columnDefinition = "bigint default 0")
    private Long add_time;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    @Column(columnDefinition = "integer default 1")
    private Integer state;
}
