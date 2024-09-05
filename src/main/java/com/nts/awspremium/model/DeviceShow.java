package com.nts.awspremium.model;

import lombok.Getter;

@Getter
public class DeviceShow {
    String device_id;
    Integer state;
    Integer running;
    Long add_time;
    Long update_time;
    Long get_time;
    Integer num_account;
    Integer num_profile;
    String profile_id;
    String platform;
    String task;
    String box_id;

    public DeviceShow(String device_id,String box_id, Integer state, Integer running, Long add_time, Long update_time, Long get_time, Integer num_account, Integer num_profile, String profile_id, String platform, String task) {
        this.device_id = device_id;
        this.box_id = box_id;
        this.state = state;
        this.running = running;
        this.add_time = add_time;
        this.update_time = update_time;
        this.get_time = get_time;
        this.num_account = num_account;
        this.num_profile = num_profile;
        this.profile_id = profile_id;
        this.platform = platform;
        this.task = task;
    }
}
