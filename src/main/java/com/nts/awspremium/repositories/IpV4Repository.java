package com.nts.awspremium.repositories;

import com.nts.awspremium.model.IpV4;
import com.nts.awspremium.model.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface IpV4Repository extends JpaRepository<IpV4,Long> {
    @Query(value = "select count(*) from ipv4 where state=1 and ipv4 LIKE ?1",nativeQuery = true)
    public Integer checkProxyLive(String ipv4);

    @Query(value = "select count(*) from ipv4 where state=1 and ipv4=?1",nativeQuery = true)
    public Integer checkIPv4Live(String ipv4);

    @Query(value = "select ipv4 from ipv4 where timereset=?1 and vps like ?2 order by usercount asc limit 1",nativeQuery = true)
    public String getIpv4ByVps(Integer timereset,String vps);

    @Query(value = "SELECT ipv4 FROM ipv4 order by usercount asc,rand() limit 1",nativeQuery = true)
    public String getIpv4ByVps();

    @Query(value = "select count(*) from ipv4 where timereset=?1 and vps like ?2 ",nativeQuery = true)
    public Integer checkIpv4ByVps(Integer timereset,String vps);

    @Query(value = "select count(*) from ipv4 where vps like ?1 ",nativeQuery = true)
    public Integer checkIpv4ByVps(String vps);
    @Query(value = "select ipv4 from ipv4 where cron=?1 order by timecheck asc",nativeQuery = true)
    public List<String> getListIpv4(Integer cron);

    @Query(value = "select id from ipv4",nativeQuery = true)
    public List<String> getIdByIpv4();

    @Query(value = "select * from ipv4",nativeQuery = true)
    public List<IpV4> getListIpv4();

    @Modifying
    @Transactional
    @Query(value = "Update ipv4 SET cron=?1 where id=?2",nativeQuery = true)
    public void updatecronIpv4(Integer cron,Long id);

    @Modifying
    @Transactional
    @Query(value = "Update ipv4 SET vps=?1,vspcount=?2 where id=?3",nativeQuery = true)
    public void updateVPSIpv4(String vps,Integer vspcount,Long id);

    @Modifying
    @Transactional
    @Query(value = "Update ipv4 SET state=1,timecheck=?1,numcheck=0 where ipv4=?2",nativeQuery = true)
    public void updateIpv4Ok(Long timecheck, String ipv4 );

    @Query(value = "SELECT * from ipv4 where ipv4=?1",nativeQuery = true)
    public List<IpV4> getStateByIpv4(String ipv4);

    @Query(value = "SELECT count(*) from ipv4 where ipv4=?1",nativeQuery = true)
    public Integer checkIpv4(String ipv4);

    @Modifying
    @Transactional
    @Query(value = "Update ipv4 SET state=0,timecheck=?1,numcheck=numcheck+1 where ipv4=?2",nativeQuery = true)
    public void updateIpv4Error(Long timecheck, String ipv4 );

    @Modifying
    @Transactional
    @Query(value = "Update ipv4 SET usercount=usercount+1 where ipv4 like ?1",nativeQuery = true)
    public void updateUserCountByIpv4(String ipv4 );

    @Modifying
    @Transactional
    @Query(value = "Update ipv4 SET vps=concat(vps,',',?1),vspcount=vspcount+1 where timereset=?2 and vspcount<5 order by vspcount asc limit 1 ",nativeQuery = true)
    public void updateIpv4byVps(String vps,Integer timereset);

    @Modifying
    @Transactional
    @Query(value = "Update ipv4 SET vps=concat(vps,',',?1),vspcount=vspcount+1 where  vspcount<5 order by vspcount asc limit 1 ",nativeQuery = true)
    public void updateIpv4byVps(String vps);


    @Modifying
    @Transactional
    @Query(value = "DELETE  FROM ipv4 where ipv4=?1",nativeQuery = true)
    public void DeleteIPv4(String ipv4);
}
