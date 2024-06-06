package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "google_key")
public class GoogleKey {
    @Id
    private String key_id;
    @Column(columnDefinition = "integer default 0")
    private Integer get_count;
    @Column(columnDefinition = "integer default 1")
    private Integer state;
    public GoogleKey() {
    }

}
