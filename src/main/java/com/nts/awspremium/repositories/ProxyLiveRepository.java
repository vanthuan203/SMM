package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.model.ProxyLive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ProxyLiveRepository extends JpaRepository<ProxyLive, Integer> {
    @Query(value = "select * from proxy where proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5 group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxy();

    @Query(value = "select * from proxylive where ipv4 in (select ipv4 from ipv4 where state=1) and running=0 and geo=?1 order by rand() limit 1",nativeQuery = true)
    public List<ProxyLive> getProxyLive(String geo);

    @Query(value = "select * from proxy where proxy NOT LIKE ?1 and proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5  group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxy(String proxy);

    @Query(value = "SELECT id FROM proxylive where proxy=?1 and vps=?2 limit 1 ",nativeQuery = true)
    public Integer getIdByProxyLive(String proxy,String vps);


    @Query(value = "SELECT id FROM proxylive where proxy=?1 and vps=?2 limit 1 ",nativeQuery = true)
    public Integer getIdByProxyLiveFalse(String proxy,String vps);


    @Modifying
    @Transactional
    @Query(value = "update proxylive set running=0,vps='' where  vps=?1 ",nativeQuery = true)
    public Integer updaterunningByVps(String vps);


    @Modifying
    @Transactional
    @Query(value = "update proxylive set running=0,vps='' where  id=?1",nativeQuery = true)
    public Integer updaterunningProxyLiveByVps(Integer id);




    @Modifying
    @Transactional
    @Query(value = "UPDATE proxylive set running=1,vps=?1,timeget=?2  where id=?3",nativeQuery = true)
    public Integer updateProxyLiveGet(String vps,Long timeget,Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE proxylive set running=0,vps='' where  round((UNIX_TIMESTAMP()-timeget/1000)/60/60) >=4 and running=1",nativeQuery = true)
    public Integer ResetProxyThan4h();
}
