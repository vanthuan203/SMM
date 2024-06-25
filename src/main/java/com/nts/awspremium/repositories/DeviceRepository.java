package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountTask;
import com.nts.awspremium.model.Device;
import com.nts.awspremium.model.OrderRunning;
import com.nts.awspremium.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device,String> {

    @Query(value = "Select * from device where device_id=?1 limit 1",nativeQuery = true)
    public Device check_DeviceId(String device_id);
    @Query(value = "select count(*) from device where device_id=?1",nativeQuery = true)
    public Integer find_Device(String device_id);

    @Query(value = "SELECT o FROM Device o")
    Page<Device> get_List_Device(Pageable pageable);
    @Query(value = "SELECT o FROM Device o where o.device_id=?1")
    Page<Device> get_List_Device(Pageable pageable,String device_id);


}
