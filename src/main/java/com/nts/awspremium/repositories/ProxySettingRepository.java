package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ProxySetting;
import com.nts.awspremium.model.Socks_IPV4;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProxySettingRepository extends JpaRepository<ProxySetting,Long> {
    @Query(value = "SELECT * FROM proxysetting where id=1",nativeQuery = true)
    public ProxySetting getProxySettingById();
}
