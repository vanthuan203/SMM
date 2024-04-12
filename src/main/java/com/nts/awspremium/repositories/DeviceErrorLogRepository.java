package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Device;
import com.nts.awspremium.model.DeviceErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeviceErrorLogRepository extends JpaRepository<DeviceErrorLog,Long> {

    @Query(value = "Select count(*) from device where device_id=?1 limit 1",nativeQuery = true)
    public Integer checkDeviceId(String device_id);
    @Query(value = "select * from device where device_id=?1",nativeQuery = true)
    public Device checkDevice(String device_id);

}
