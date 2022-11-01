package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name="ipv4")
public class IpV4 {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String ipv4;
    private Integer state;
    private Long timecheck;
    public IpV4() {
    }

    public IpV4(long id, String ipv4, Integer state, Long timecheck) {
        this.id = id;
        this.ipv4 = ipv4;
        this.state = state;
        this.timecheck = timecheck;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getTimecheck() {
        return timecheck;
    }

    public void setTimecheck(Long timecheck) {
        this.timecheck = timecheck;
    }
}
