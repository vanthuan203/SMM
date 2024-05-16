package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountRegTiktok;
import com.nts.awspremium.model.Device;
import com.nts.awspremium.model.Vps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device,String> {

    @Query(value = "Select count(*) from device where device_id=?1 limit 1",nativeQuery = true)
    public Integer checkDeviceId(String device_id);
    @Query(value = "select * from device where device_id=?1",nativeQuery = true)
    public Device checkDevice(String device_id);

}
