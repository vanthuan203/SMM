package com.nts.awspremium.model;

import lombok.Getter;

@Getter
public class DeviceShow {
    String device_id;
    Integer state;
    Long ip_changer_time;
    String ip_address;
    Integer status;
    Integer running;
    Long add_time;
    Long update_time;
    Long get_time;
    Integer num_account;
    String account_live;
    String account_die;
    Integer num_profile;
    Integer num_profile_set;
    String profile_id;
    String platform;
    String task;
    String box_id;
    String rom_version;
    String mode;


    public DeviceShow(String device_id,String box_id,String rom_version,String mode, Integer state,String ip_address,Long ip_changer_time, Integer status, Integer running, Long add_time, Long update_time, Long get_time, Integer num_account,String account_live,String account_die, Integer num_profile,Integer num_profile_set, String profile_id, String platform, String task) {
        this.device_id = device_id;
        this.box_id = box_id;
        this.rom_version = rom_version;
        this.mode = mode;
        this.state = state;
        this.ip_address = ip_address;
        this.ip_changer_time = ip_changer_time;
        this.status = status;
        this.running = running;
        this.add_time = add_time;
        this.update_time = update_time;
        this.get_time = get_time;
        this.num_account = num_account;
        this.account_live = account_live;
        this.account_die = account_die;
        this.num_profile = num_profile;
        this.num_profile_set = num_profile_set;
        this.profile_id = profile_id;
        this.platform = platform;
        this.task = task;
    }
}
