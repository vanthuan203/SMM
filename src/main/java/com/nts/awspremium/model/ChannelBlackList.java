package com.nts.awspremium.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "channel_youtube_blacklist")
public class ChannelBlackList {
    @Id
    private String channel_id;

    public ChannelBlackList() {
    }

    public ChannelBlackList(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }
}