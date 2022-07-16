package com.nts.awspremium.model;

public class ChannelOrder {
    private String channel_id;
    private String list_video;
    private Integer home_rate;
    private Integer direct_rate;
    private Integer search_rate;
    private Integer suggest_rate;
    private Integer enabled;
    private Integer view_percent;
    private Integer max_thread;

    public ChannelOrder() {
    }

    public ChannelOrder(String channel_id, String list_video, Integer home_rate, Integer direct_rate, Integer search_rate, Integer suggest_rate, Integer enabled, Integer view_percent, Integer max_thread) {
        this.channel_id = channel_id;
        this.list_video = list_video;
        this.home_rate = home_rate;
        this.direct_rate = direct_rate;
        this.search_rate = search_rate;
        this.suggest_rate = suggest_rate;
        this.enabled = enabled;
        this.view_percent = view_percent;
        this.max_thread = max_thread;
    }

    @Override
    public String toString() {
        return "ChannelOrder{" +
                "channel_id='" + channel_id + '\'' +
                ", list_video='" + list_video + '\'' +
                ", home_rate=" + home_rate +
                ", direct_rate=" + direct_rate +
                ", search_rate=" + search_rate +
                ", suggest_rate=" + suggest_rate +
                ", enabled=" + enabled +
                ", view_percent=" + view_percent +
                ", max_thread=" + max_thread +
                '}';
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getList_video() {
        return list_video;
    }

    public void setList_video(String list_video) {
        this.list_video = list_video;
    }

    public Integer getHome_rate() {
        return home_rate;
    }

    public void setHome_rate(Integer home_rate) {
        this.home_rate = home_rate;
    }

    public Integer getDirect_rate() {
        return direct_rate;
    }

    public void setDirect_rate(Integer direct_rate) {
        this.direct_rate = direct_rate;
    }

    public Integer getSearch_rate() {
        return search_rate;
    }

    public void setSearch_rate(Integer search_rate) {
        this.search_rate = search_rate;
    }

    public Integer getSuggest_rate() {
        return suggest_rate;
    }

    public void setSuggest_rate(Integer suggest_rate) {
        this.suggest_rate = suggest_rate;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getView_percent() {
        return view_percent;
    }

    public void setView_percent(Integer view_percent) {
        this.view_percent = view_percent;
    }

    public Integer getMax_thread() {
        return max_thread;
    }

    public void setMax_thread(Integer max_thread) {
        this.max_thread = max_thread;
    }
}
