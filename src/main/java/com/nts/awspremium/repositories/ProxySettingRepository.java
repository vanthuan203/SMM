package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ProxySetting;
import com.nts.awspremium.model.Socks_IPV4;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProxySettingRepository extends JpaRepository<ProxySetting,Long> {
    @Query(value = "SELECT * FROM proxysetting where id=1",nativeQuery = true)
    public ProxySetting getProxySettingById();
    @Query(value = "SELECT * FROM proxysetting where option_proxy in(select option_setting from ipv4 where ipv4=?1) limit 1",nativeQuery = true)
    public ProxySetting getProxySettingByOption(String ipv4);

    @Query(value = "SELECT * FROM proxysetting where id=?1",nativeQuery = true)
    public ProxySetting getProxySettingById(Long id);

    @Query(value = "SELECT * FROM proxysetting order by id",nativeQuery = true)
    public List<ProxySetting> getProxySetting();

}
