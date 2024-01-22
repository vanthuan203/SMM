package com.nts.awspremium.model;

import javax.persistence.*;

@Entity
@Table(name = "authenipv4")
public class AuthenIPv4 {
    @Id
    private String ipv4;
    private Long timecheck;
    private Integer lockmode=0;
    private Long timeadd;
    public AuthenIPv4() {
    }

    public AuthenIPv4(String ipv4, Long timecheck, Integer lockmode, Long timeadd) {
        this.ipv4 = ipv4;
        this.timecheck = timecheck;
        this.lockmode = lockmode;
        this.timeadd = timeadd;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public Long getTimecheck() {
        return timecheck;
    }

    public void setTimecheck(Long timecheck) {
        this.timecheck = timecheck;
    }

    public Integer getLockmode() {
        return lockmode;
    }

    public void setLockmode(Integer lockmode) {
        this.lockmode = lockmode;
    }

    public Long getTimeadd() {
        return timeadd;
    }

    public void setTimeadd(Long timeadd) {
        this.timeadd = timeadd;
    }
}
