package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountRegTiktok;
import com.nts.awspremium.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeviceRepository extends JpaRepository<Device,String> {

    @Query(value = "Select count(*) from device where device_id=?1 limit 1",nativeQuery = true)
    public Integer checkDeviceId(String device_id);
    @Query(value = "call update_running_acc_reg_tiktok(?1,?2,?3,?4)",nativeQuery = true)
    public AccountRegTiktok getAccountRegTiktok(String vps,String device_id,Long time_check,String code);

}
