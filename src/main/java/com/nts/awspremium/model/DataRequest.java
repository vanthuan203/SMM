package com.nts.awspremium.model;

public class DataRequest {
    private String key;
    private String link;
    private int quantity;
    private String action;
    private int service=-1;
    private Long order=-1L;
    private String orders="";
    private String list="";
    private String search="";
    private String suggest="";

    public DataRequest() {
    }

    public DataRequest(String key, String link, int quantity, String action, int service) {
        this.key = key;
        this.link = link;
        this.quantity = quantity;
        this.action = action;
        this.service = service;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getSuggest() {
        return suggest;
    }

    public void setSuggest(String suggest) {
        this.suggest = suggest;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public String getOrders() {
        return orders;
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public String getKey() {
        return key;
    }



    public void setKey(String key) {
        this.key = key;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getService() {
        return service;
    }

    public void setService(int service) {
        this.service = service;
    }
}
