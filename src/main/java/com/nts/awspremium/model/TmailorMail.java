package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "tmailor_mail")
public class TmailorMail implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String email;
    @Column(columnDefinition = "varchar(5555) default ''")
    private String refresh_token="";
    @Column(columnDefinition = "varchar(5555) default ''")
    private String access_token="";
}
