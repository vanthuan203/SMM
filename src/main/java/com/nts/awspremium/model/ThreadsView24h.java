package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "threads_view_24h")
public class ThreadsView24h {
    @Id
    private String id;
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
}