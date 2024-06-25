package com.nts.awspremium.repositories;

import com.nts.awspremium.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeviceRepository extends JpaRepository<Device,String> {

    @Query(value = "Select * from device where device_id=?1 limit 1",nativeQuery = true)
    public Device check_DeviceId(String device_id);
    @Query(value = "select count(*) from device where device_id=?1",nativeQuery = true)
    public Integer find_Device(String device_id);

    @Query(value = "SELECT new com.nts.awspremium.model.DeviceShow(d.device_id,d.state,MAX(a.running),d.add_time,d.update_time,MAX(a.get_time),d.num_account,d.num_profile,a.profile.profile_id,a.platform,a.task) FROM Device d left join AccountTask  a on a.device.device_id=d.device_id and a.running=1 group by d.device_id")
    Page<DeviceShow> get_List_Device(Pageable pageable);
    @Query(value = "SELECT new com.nts.awspremium.model.DeviceShow(d.device_id,d.state,a.running,d.add_time,d.update_time,a.get_time,d.num_account,d.num_profile,a.profile.profile_id,a.platform,a.task) " +
            "FROM Device d left join AccountTask  a on a.device.device_id=d.device_id where d.device_id=?1 group by d.device_id")
    Page<DeviceShow> get_List_Device(Pageable pageable, String device_id);


}
