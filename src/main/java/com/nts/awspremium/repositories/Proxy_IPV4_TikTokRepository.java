package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.model.Proxy_IPV4_TikTok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface Proxy_IPV4_TikTokRepository extends JpaRepository<Proxy_IPV4_TikTok, String> {
    @Query(value = "select * from proxy_ipv4_tiktok  order by running asc,rand() limit 1",nativeQuery = true)
    public Proxy_IPV4_TikTok getProxyFixAccountTikTok();

    @Query(value = "select proxy from proxy_ipv4_tiktok where ipv4 in(select ipv4 from ipv4 where state=1)  order by rand() limit 1",nativeQuery = true)
    public String getProxyRandTikTok();
    @Modifying
    @Transactional
    @Query(value = "UPDATE proxy_ipv4_tiktok SET running=IF(running<=0,0,running-1) where proxy=?1",nativeQuery = true)
    public Integer resetProxyByProxyId(String proxy);


    @Modifying
    @Transactional
    @Query(value = "UPDATE proxy_ipv4_tiktok SET running=IF(running<=0,0,running-1),device_id='' where proxy in (select proxy from account_reg_tiktok where proxy!='' and running=0)",nativeQuery = true)
    public Integer resetProxyByAccountRegTiktok();
}
