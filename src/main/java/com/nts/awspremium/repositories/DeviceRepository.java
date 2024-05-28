package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountTask;
import com.nts.awspremium.model.Device;
import com.nts.awspremium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device,String> {

    @Query(value = "Select * from device where device_id=?1 limit 1",nativeQuery = true)
    public Device check_DeviceId(String device_id);
    @Query(value = "select count(*) from device where device_id=?1",nativeQuery = true)
    public Integer find_Device(String device_id);





}
