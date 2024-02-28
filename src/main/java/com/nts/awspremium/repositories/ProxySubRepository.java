package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.model.ProxyLive;
import com.nts.awspremium.model.ProxySub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ProxySubRepository extends JpaRepository<ProxySub, Integer> {
    @Query(value = "select * from proxysub where proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5 group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxy();

    @Query(value = "select * from proxysub where state=1 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyUpdate();

    @Query(value = "select * from proxysub where state=1 and typeproxy like ?1 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyBuff(String typeproxy);
/*
    @Query(value = "select * from proxy where state=1 and  INSTR(typeproxy,(select geo from account where username=?1))>0 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyBuffByUsername(String username);


 */
    @Query(value = "select * from proxysub where state=1 and running=0 and INSTR(typeproxy,(select geo from account where username=?1 limit 1))>0  order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyBuffByUsername(String username);

    @Query(value = "select * from proxysub where state=1 and running=0 and INSTR(typeproxy,'vt')>0 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyVtBuffTest();

    @Query(value = "select * from proxysub where state=1 and running=0 and INSTR(typeproxy,'vt')>0 and ipv4 NOT LIKE ?1 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyVtBuffTest(String ipv4);
    @Query(value = "select * from proxysub where state=1 and running=0 and INSTR(typeproxy,'hc5p')>0 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyHc5pBuffTest();
    @Query(value = "select * from proxysub where state=1 and running=0 and INSTR(typeproxy,'hc5p')>0 and ipv4 NOT LIKE ?1 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyHc5pBuffTest(String ipv4);
    @Query(value = "select * from proxysub where state=1 and running=0 and INSTR(typeproxy,'hcport')>0 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyHcPortBuffTest();

    @Query(value = "select * from proxysub where state=1 and running=0 and INSTR(typeproxy,'hcport')>0 and ipv4 NOT LIKE ?1 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyHcPortBuffTest(String ipv4);
    @Query(value = "SELECT ipv4.ipv4,count(*) as totalport,ipv4.timecheck,ipv4.state,proxysub.geo,ipv4.numcheck,proxysub.typeproxy FROM ipv4 left join proxysub on ipv4.ipv4=proxysub.ipv4 group by ipv4.ipv4  order by numcheck desc;",nativeQuery = true)
    public List<String> getListProxyV4();
/*
    @Query(value = "select * from proxy where state=1 and  INSTR(typeproxy,(select geo from account where username=?1))>0 and proxy NOT LIKE ?2 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyBuffByIpv4ByUsername(String username,String proxy);

 */
    @Query(value = "select * from proxysub where state=1 and running=0 and INSTR(typeproxy,(select geo from account where username=?1 limit 1))>0 and ipv4 NOT LIKE ?2 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyBuffByIpv4ByUsername(String username,String proxy);
    @Query(value = "select * from proxysub where state=1 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyBuff();

    @Query(value = "SELECT ipv4 FROM AccPremium.proxysub where typeproxy not like '%vn%' group by ipv4 ",nativeQuery = true)
    public List<String> getIpv4ProxyBuff();

    @Query(value = "select * from proxysub where state=1 and proxy NOT LIKE ?1 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyBuffByIpv4(String proxy);

    @Query(value = "select * from proxysub where state=1 and typeproxy like ?1 and proxy NOT LIKE ?2 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyBuffByIpv4(String typeproxy,String proxy);
    @Query(value = "select * from proxysub where state=1 and proxy NOT LIKE ?1 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyUpdate(String proxy);

    @Query(value = "select * from proxysub where state=1 and running=0 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxySubT1();

    @Query(value = "select * from proxysub where ipv4 in (select ipv4 from ipv4 where state=1) and running=0 and geo=?1 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyByGeo(String geo);

    @Query(value = "select * from proxysub where running=0 and geo=?1 order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyFixAccountByGeo(String geo);

    @Query(value = "select proxy from proxysub where running=0 and geo=?1 order by rand() limit 1",nativeQuery = true)
    public String getProxyRandByGeo(String geo);

    @Query(value = "select * from proxysub where ipv4 in(select ipv4 from ipv4 where state=1) and running=0 and geo='live' order by rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyByGeoNoCheckTime();

    @Query(value = "select * from proxylive where ipv4 in (select ipv4 from ipv4 where state=1) and running=0 and geo=?1 order by rand() limit 1",nativeQuery = true)
    public List<ProxyLive> getProxyLive(String geo);

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

    @Query(value = "select * from proxysub where proxy NOT LIKE ?1 and proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5  group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxy(String proxy);

    @Query(value = "select * from proxysub where proxy NOT LIKE ?1 and proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5  group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<ProxySub> getProxyTimeGetNull(String proxy);

    @Query(value = "SELECT * FROM proxysub where proxy=?1 limit 1",nativeQuery = true)
    public List<ProxySub> findProxy(String proxy);

    @Query(value = "select count(*) from INFORMATION_SCHEMA.PROCESSLIST where db = 'AccSub' and COMMAND='Query' and TIME>0",nativeQuery = true)
    public Integer PROCESSLISTSUB();

    @Query(value = "select count(*) from INFORMATION_SCHEMA.PROCESSLIST where db = 'AccSub' and COMMAND='Query'",nativeQuery = true)
    public Integer PROCESSLISTSUBGETACC();

    @Query(value = "SELECT count(*) FROM proxy where proxy=?1",nativeQuery = true)
    public Integer checkproxynull(String proxy);

    @Query(value = "SELECT count(*) FROM proxy where state=1 and",nativeQuery = true)
    public Integer countProxy();

    @Query(value = "SELECT id FROM proxy where proxy=?1 and vps=?2 limit 1 ",nativeQuery = true)
    public Integer getIdByProxy(String proxy,String vps);

    @Query(value = "SELECT id FROM proxy where proxy=?1 and vps=?2 limit 1 ",nativeQuery = true)
    public Integer getIdByProxyLive(String proxy,String vps);

    @Query(value = "SELECT running FROM proxy where id=?1 limit 1 ",nativeQuery = true)
    public Integer getRunningProxyById(Integer id);

    @Query(value = "SELECT running FROM proxylive where id=?1 limit 1 ",nativeQuery = true)
    public Integer getRunningProxyLiveById(Integer id);

    @Query(value = "SELECT id FROM proxy where proxy=?1 and vps=?2 limit 1 ",nativeQuery = true)
    public Integer getIdByProxyFalse(String proxy,String vps);

    @Query(value = "SELECT id FROM proxylive where proxy=?1 and vps=?2 limit 1 ",nativeQuery = true)
    public Integer getIdByProxyLiveFalse(String proxy,String vps);

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
    @Query(value = "update AccPremium.proxysub set vps='',running=0 where proxy in (SELECT proxy FROM AccPremium.account where geo=?1);",nativeQuery = true)
    public Integer resetProxyByGeo(String geo);

    @Modifying
    @Transactional
    @Query(value = "update proxy set running=0,vps='' where  proxy not in(select proxy from history where running=1) and running=1",nativeQuery = true)
    public Integer ressetRunningProxyError();



    @Modifying
    @Transactional
    @Query(value = "update proxy set typeproxy='pending' where ipv4=?1 limit 250",nativeQuery = true)
    public Integer updatepending(String ipv4);

    @Modifying
    @Transactional
    @Query(value = "update proxy set running=0,vps='' where id=?1",nativeQuery = true)
    public Integer updaterunning(Integer proxyId);

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

    @Query(value = "select count(*) from INFORMATION_SCHEMA.PROCESSLIST where db = 'AccSub' and COMMAND='Query' and TIME>0",nativeQuery = true)
    public Integer PROCESSLISTVIEW();

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

    @Modifying
    @Transactional
    @Query(value = "UPDATE proxy set running=0,vps='' where  round((UNIX_TIMESTAMP()-timeget/1000)/60/60) >=1 and running=1",nativeQuery = true)
    public Integer ResetProxyThan2h();
}