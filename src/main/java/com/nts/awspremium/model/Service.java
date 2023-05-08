package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "service")
public class Service {
    @Id
    private Integer service;
    private String name;
    private String type;
    private String category;
    private Float rate;
    private Integer min;
    private Integer max;
    private Integer thread;
    private String note;
    private String geo;
    private Integer enabled;
    private Integer maxorder;
    private Integer search;
    private Integer suggest;
    private Integer dtn;
    private Integer mintime;
    private Integer maxtime;
    private Integer refill;
    private Integer maxtimerefill;
    private Integer checktime;
    public Service() {
    }

    public Service(Integer service, String name, String type, String category, Float rate, Integer min, Integer max, Integer thread, String note, String geo, Integer enabled, Integer maxorder, Integer search, Integer suggest, Integer dtn, Integer mintime, Integer maxtime, Integer refill, Integer maxtimerefill, Integer checktime) {
        this.service = service;
        this.name = name;
        this.type = type;
        this.category = category;
        this.rate = rate;
        this.min = min;
        this.max = max;
        this.thread = thread;
        this.note = note;
        this.geo = geo;
        this.enabled = enabled;
        this.maxorder = maxorder;
        this.search = search;
        this.suggest = suggest;
        this.dtn = dtn;
        this.mintime = mintime;
        this.maxtime = maxtime;
        this.refill = refill;
        this.maxtimerefill = maxtimerefill;
        this.checktime = checktime;
    }

    public Integer getMaxorder() {
        return maxorder;
    }

    public void setMaxorder(Integer maxorder) {
        this.maxorder = maxorder;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getService() {
        return service;
    }

    public void setService(Integer service) {
        this.service = service;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getSearch() {
        return search;
    }

    public void setSearch(Integer search) {
        this.search = search;
    }

    public Integer getSuggest() {
        return suggest;
    }

    public void setSuggest(Integer suggest) {
        this.suggest = suggest;
    }

    public Integer getDtn() {
        return dtn;
    }

    public void setDtn(Integer dtn) {
        this.dtn = dtn;
    }

    public Integer getMintime() {
        return mintime;
    }

    public void setMintime(Integer mintime) {
        this.mintime = mintime;
    }

    public Integer getMaxtime() {
        return maxtime;
    }

    public void setMaxtime(Integer maxtime) {
        this.maxtime = maxtime;
    }

    public Integer getRefill() {
        return refill;
    }

    public void setRefill(Integer refill) {
        this.refill = refill;
    }

    public Integer getMaxtimerefill() {
        return maxtimerefill;
    }

    public void setMaxtimerefill(Integer maxtimerefill) {
        this.maxtimerefill = maxtimerefill;
    }

    public Integer getChecktime() {
        return checktime;
    }

    public void setChecktime(Integer checktime) {
        this.checktime = checktime;
    }
}
