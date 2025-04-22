package com.nts.awspremium.repositories;

import com.nts.awspremium.model.DeviceIp;
import com.nts.awspremium.model.IpSum;
import com.nts.awspremium.model.TikTokViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IpSumRepository extends JpaRepository<IpSum,String> {
    @Query(value = "SELECT * FROM ip_sum where ip=?1 limit 1",nativeQuery = true)
    public IpSum get_By_IP(String ip);
}
