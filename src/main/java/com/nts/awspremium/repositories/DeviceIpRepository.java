package com.nts.awspremium.repositories;

import com.nts.awspremium.model.DeviceIp;
import com.nts.awspremium.model.IpSum;
import com.nts.awspremium.model.TikTokViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeviceIpRepository extends JpaRepository<DeviceIp,String> {
    @Query(value = "SELECT * FROM device_ip where device_id=?1 limit 1",nativeQuery = true)
    public DeviceIp get_By_DeviceId(String device_id);

}
