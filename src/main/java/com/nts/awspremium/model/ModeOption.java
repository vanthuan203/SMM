package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "mode_option")
public class ModeOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String mode;
    @Column(columnDefinition = "varchar(255) default ''")
    private String platform="";
    @Column(columnDefinition = "varchar(255) default ''")
    private String task="";
    @Column(columnDefinition = "integer default 0")
    private Integer max_task;
    @Column(columnDefinition = "integer default 0")
    private Integer priority;
    @Column(columnDefinition = "integer default 0")
    private Integer state;
    @Column(columnDefinition = "integer default 0")
    private Integer time_get_task=0;
    @Column(columnDefinition = "integer default 0")
    private Integer time_waiting_task=0;
    @Column(columnDefinition = "integer default 0")
    private Integer day_true_task=7;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
    public ModeOption() {
    }
}
