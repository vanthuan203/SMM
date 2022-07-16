package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ipv4")
public class IpV4 {
    @Id
    private long id;
    private String ipv4;
    private Integer state;

    public IpV4() {
    }

    public IpV4(long id, String ipv4, Integer state) {
        this.id = id;
        this.ipv4 = ipv4;
        this.state = state;
    }

    @Override
    public String toString() {
        return "IpV4{" +
                "id=" + id +
                ", ipv4='" + ipv4 + '\'' +
                ", state=" + state +
                '}';
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
}
