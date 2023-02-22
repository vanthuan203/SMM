package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dataorder")
public class DataOrder {
    @Id
    private Long orderid;
    private String listkey;
    private String listvideo;

    public DataOrder() {
    }

    public DataOrder(Long orderid, String listkey, String listvideo) {
        this.orderid = orderid;
        this.listkey = listkey;
        this.listvideo = listvideo;
    }

    public String getListkey() {
        return listkey;
    }

    public void setListkey(String listkey) {
        this.listkey = listkey;
    }

    public String getListvideo() {
        return listvideo;
    }

    public void setListvideo(String listvideo) {
        this.listvideo = listvideo;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }
}
