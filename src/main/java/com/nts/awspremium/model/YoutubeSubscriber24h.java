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
@Table(name = "youtube_subscriber_24h")
public class YoutubeSubscriber24h {
    @Id
    private String id;
    @Column(columnDefinition = "varchar(255) default ''")
    private String device_id="";
    @Column(columnDefinition = "bigint default 0")
    private Long update_time;
}