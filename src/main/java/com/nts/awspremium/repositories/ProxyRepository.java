package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.model.ProxyLive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ProxyRepository extends JpaRepository<Proxy, Integer> {
    @Query(value = "select * from proxy where proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5 group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxy();

    @Query(value = "SELECT ipv4.ipv4,count(*) as totalport,ipv4.timecheck,ipv4.state,proxy.geo,ipv4.numcheck,proxy.typeproxy FROM ipv4 left join proxy on ipv4.ipv4=proxy.ipv4 group by ipv4.ipv4  order by numcheck desc;",nativeQuery = true)
    public List<String> getListProxyV4();

    @Query(value = "SELECT ipv4 FROM AccPremium.proxy where typeproxy not like '%vn%' group by ipv4 ",nativeQuery = true)
    public List<String> getIpv4ProxyBuff();


    @Query(value = "select * from proxy where state=1 and running=0 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20 order by rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxySubT1();

    @Query(value = "select * from proxy where ipv4 in (select ipv4 from ipv4 where state=1) and running=0 and geo=?1 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20 order by rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyByGeo(String geo);

    @Query(value = "select * from proxy where running=0 and geo=?1 order by rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyFixAccountByGeo(String geo);

    @Query(value = "select proxy from proxy where running=0 and geo=?1 order by rand() limit 1",nativeQuery = true)
    public String getProxyRandByGeo(String geo);

    @Query(value = "select * from proxy where ipv4 in(select ipv4 from ipv4 where state=1) and running=0 and geo='live' order by rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyByGeoNoCheckTime();

    @Query(value = "select * from proxy where ipv4 in (select ipv4 from ipv4 where state=1) and running=0 and geo=?1 order by rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyNotRunningAndLive(String geo);

    @Query(value = "select * from proxy where ipv4 in (select ipv4 from ipv4 where state=1) and running=0 and geo='sub' and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=10 order by rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyAccSub();

    @Query(value = "select * from proxy where  ipv4 in (select ipv4 from ipv4 where state=1 and timereset=30) and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=60 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxySubT2();

    @Query(value = "SELECT * from proxy where state=1 and ipv4 like ?1 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20 order by timeget asc,rand() limit 1;",nativeQuery = true)
    public List<Proxy> getProxySubByIpv4T1(String ipv4);
    @Query(value = "SELECT * from proxy where proxy like ?1 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=60 order by timeget asc,rand() limit 1;",nativeQuery = true)
    public List<Proxy> getProxySubByIpv4T2(String ipv4);

    @Query(value = "select * from proxy where proxy NOT LIKE ?1 and proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5  group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxy(String proxy);


    @Query(value = "SELECT count(*) FROM proxy where proxy=?1",nativeQuery = true)
    public Integer checkproxynull(String proxy);


    @Query(value = "SELECT id FROM proxy where proxy=?1 and vps=?2 limit 1 ",nativeQuery = true)
    public Integer getIdByProxyLive(String proxy,String vps);

    @Query(value = "SELECT running FROM proxy where id=?1 limit 1 ",nativeQuery = true)
    public Integer getRunningProxyById(Integer id);

    @Query(value = "SELECT id FROM proxy where proxy=?1 and vps=?2 limit 1 ",nativeQuery = true)
    public Integer getIdByProxyFalse(String proxy,String vps);

    @Query(value = "SELECT count(*) FROM proxy where state=?1 and ipv4=?2 limit 1",nativeQuery = true)
    public Integer checkState(Integer state,String ipv4);

    @Query(value = "SELECT count(*) FROM proxy where state=1 and proxy=(select proxy from account where username=?1 limit 1) limit 1",nativeQuery = true)
    public Integer checkProxyLiveByUsername(String username);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxyhistory where round((UNIX_TIMESTAMP()-id/1000)/60/60) >3",nativeQuery = true)
    public Integer deleteProxyHisThan24h();


    @Modifying
    @Transactional
    @Query(value = "update proxy set typeproxy='pending' where ipv4=?1 limit 250",nativeQuery = true)
    public Integer updatepending(String ipv4);


    @Modifying
    @Transactional
    @Query(value = "update proxy set running=0,vps='' where  vps=?1 ",nativeQuery = true)
    public Integer updaterunningByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "update proxy set running=0,vps='' where  id=?1",nativeQuery = true)
    public Integer updaterunningProxyByVps(Integer id);


    @Modifying
    @Transactional
    @Query(value = "update proxy set running=0,vps='' where  id=?1",nativeQuery = true)
    public Integer updaterunningProxyLiveByVps(Integer id);


    @Modifying
    @Transactional
    @Query(value = "update proxy set running=0,vps='' where  proxy=?1",nativeQuery = true)
    public Integer updaterunningProxy(String proxy);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxy where ipv4=?1 ",nativeQuery = true)
    public Integer deleteProxyByIpv4(String ipv4);

    @Modifying
    @Transactional
    @Query(value = "UPDATE proxy set state=?1  where ipv4=?2 ",nativeQuery = true)
    public Integer updateState(Integer state,String ipv4);

    @Modifying
    @Transactional
    @Query(value = "UPDATE proxy set running=1,vps=?1,timeget=?2  where id=?3",nativeQuery = true)
    public Integer updateProxyGet(String vps,Long timeget,Integer id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE proxylive set running=1,vps=?1,timeget=?2  where id=?3",nativeQuery = true)
    public Integer updateProxyLiveGet(String vps,Long timeget,Integer id);

}
