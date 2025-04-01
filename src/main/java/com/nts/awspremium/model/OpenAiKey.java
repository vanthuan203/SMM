package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "open_ai_key")
@Getter
@Setter
public class OpenAiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String open_ai_key;
    private Long count_get;
    private Integer state;

}
