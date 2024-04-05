package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ProxySub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ProxySubRepository extends JpaRepository<ProxySub, Integer> {
    @Query(value = "SELECT ipv4.ipv4,count(*) as totalport,ipv4.timecheck,ipv4.state,proxysub.geo,ipv4.numcheck,proxysub.typeproxy FROM ipv4 left join proxysub on ipv4.ipv4=proxysub.ipv4 where geo='sub' group by ipv4.ipv4  order by geo desc, numcheck desc;",nativeQuery = true)
    public List<String> getListProxyV4();

    @Query(value = "SELECT ipv4 FROM AccPremium.proxysub where typeproxy not like '%vn%' group by ipv4 ",nativeQuery = true)
    public List<String> getIpv4ProxyBuff();

    @Query(value = "select * from proxysub where state=1 and running=0 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxySubT1();

    @Query(value = "select * from proxysub where ipv4 in (select ipv4 from ipv4 where state=1) and running=0 and geo=?1 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyByGeo(String geo);

    @Query(value = "select * from proxysub where ipv4 in(select ipv4 from ipv4 where state=1) and running=0 and geo='live' order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyByGeoNoCheckTime();

    @Query(value = "select * from proxysub where ipv4 in (select ipv4 from ipv4 where state=1) and running=0 and geo=?1 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyNotRunningAndLive(String geo);

    @Query(value = "select * from proxysub where ipv4 in (select ipv4 from ipv4 where state=1) and running=0 and geo='sub' and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=10 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyAccSub();

    @Query(value = "select * from proxysub where  ipv4 in (select ipv4 from ipv4 where state=1 and timereset=30) and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=60 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxySubT2();

    @Query(value = "SELECT * from proxysub where state=1 and ipv4 like ?1 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20 order by timeget asc,rand() limit 1;",nativeQuery = true)
    public List<ProxySub> getProxySubByIpv4T1(String ipv4);
    @Query(value = "SELECT * from proxysub where proxy like ?1 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=60 order by timeget asc,rand() limit 1;",nativeQuery = true)
    public List<ProxySub> getProxySubByIpv4T2(String ipv4);

    @Query(value = "SELECT count(*) FROM proxysub where proxy=?1",nativeQuery = true)
    public Integer checkproxynull(String proxy);

    @Query(value = "SELECT id FROM proxysub where proxy=?1 and vps=?2 limit 1 ",nativeQuery = true)
    public Integer getIdByProxyLive(String proxy,String vps);

    @Query(value = "SELECT running FROM proxysub where id=?1 limit 1 ",nativeQuery = true)
    public Integer getRunningProxyById(Integer id);
    @Query(value = "SELECT proxy FROM proxysub where ipv4=?1 limit 1;",nativeQuery = true)
    public String getProxyByIpv4(String ipv4);

    @Query(value = "SELECT id FROM proxysub where proxy=?1 and vps=?2 limit 1 ",nativeQuery = true)
    public Integer getIdByProxyFalse(String proxy,String vps);


    @Query(value = "SELECT count(*) FROM proxysub where state=?1 and ipv4=?2 limit 1",nativeQuery = true)
    public Integer checkState(Integer state,String ipv4);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxyhistory where round((UNIX_TIMESTAMP()-id/1000)/60/60) >3",nativeQuery = true)
    public Integer deleteProxyHisThan24h();

    @Modifying
    @Transactional
    @Query(value = "update proxysub set typeproxy='pending' where ipv4=?1 limit 250",nativeQuery = true)
    public Integer updatepending(String ipv4);

    @Modifying
    @Transactional
    @Query(value = "update proxysub set running=0,vps='' where  vps=?1 ",nativeQuery = true)
    public Integer updaterunningByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "update proxysub set running=0,vps='' where  id=?1",nativeQuery = true)
    public Integer updaterunningProxyByVps(Integer id);


    @Modifying
    @Transactional
    @Query(value = "update proxysub set running=0,vps='' where  id=?1",nativeQuery = true)
    public Integer updaterunningProxyLiveByVps(Integer id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxysub where ipv4=?1 ",nativeQuery = true)
    public Integer deleteProxyByIpv4(String ipv4);

    @Modifying
    @Transactional
    @Query(value = "UPDATE proxysub set state=?1  where ipv4=?2 ",nativeQuery = true)
    public Integer updateState(Integer state,String ipv4);

    @Modifying
    @Transactional
    @Query(value = "UPDATE proxysub set running=1,vps=?1,timeget=?2  where id=?3",nativeQuery = true)
    public Integer updateProxyGet(String vps,Long timeget,Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE proxysub set running=0,vps='' where  round((UNIX_TIMESTAMP()-timeget/1000)/60/60) >=1 and running=1",nativeQuery = true)
    public Integer ResetProxyThan1h();
}
