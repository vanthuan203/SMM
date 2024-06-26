package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "log_error")
public class LogError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String method_name;
    private Integer line_number;
    private String class_name;
    private String file_name;
    private String message;
    private Long add_time;
    private String date_time;
    public LogError() {
    }

}
