package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "autorefill")
public class AutoRefill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer start;
    private Integer end;
    private Integer cron;
    private Integer enabled;

    private Integer timestart;
    private Integer timend;

    private Long timelastrun;
    private Integer totalrefill;
    private Integer limitorder;


    public AutoRefill() {
    }

    public AutoRefill(Long id, Integer start, Integer end, Integer cron, Integer enabled, Integer timestart, Integer timend, Long timelastrun, Integer totalrefill, Integer limitorder) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.cron = cron;
        this.enabled = enabled;
        this.timestart = timestart;
        this.timend = timend;
        this.timelastrun = timelastrun;
        this.totalrefill = totalrefill;
        this.limitorder = limitorder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getCron() {
        return cron;
    }

    public void setCron(Integer cron) {
        this.cron = cron;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getTimestart() {
        return timestart;
    }

    public void setTimestart(Integer timestart) {
        this.timestart = timestart;
    }

    public Integer getTimend() {
        return timend;
    }

    public void setTimend(Integer timend) {
        this.timend = timend;
    }

    public Long getTimelastrun() {
        return timelastrun;
    }

    public void setTimelastrun(Long timelastrun) {
        this.timelastrun = timelastrun;
    }

    public Integer getTotalrefill() {
        return totalrefill;
    }

    public void setTotalrefill(Integer totalrefill) {
        this.totalrefill = totalrefill;
    }

    public Integer getLimitorder() {
        return limitorder;
    }

    public void setLimitorder(Integer limitorder) {
        this.limitorder = limitorder;
    }
}
