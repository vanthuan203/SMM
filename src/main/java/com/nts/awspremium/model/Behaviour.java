package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "behaviour")
public class Behaviour {
    @Id
    private String id;
    private String group_action;
    private String action;
    private Float prioritize;
    private Long next_rule;
    public Behaviour() {
    }
}
