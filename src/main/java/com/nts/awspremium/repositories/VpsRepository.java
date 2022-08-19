package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Vps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VpsRepository extends JpaRepository<Vps,Integer> {
    @Query(value = "select * from vps order by vpsoption desc,timecheck asc",nativeQuery = true)
    public List<Vps> getListVPS();
    @Query(value = "select * from vps order by CAST(SUBSTRING(vps,position('-' in vps),length(vps)) AS UNSIGNED) desc",nativeQuery = true)
    public List<Vps> getListVPSSub();

    @Query(value = "select * from vps where vps like ?1",nativeQuery = true)
    public List<Vps> findVPS(String vps);

    @Query(value = "SELECT * FROM AccSub.vps where round((UNIX_TIMESTAMP()-timecheck/1000)/60) >=5 order by CAST(SUBSTRING(vps,position('-' in vps),length(vps)) AS UNSIGNED) desc;",nativeQuery = true)
    public List<Vps> findVPSDie();

    @Query(value = "SELECT timereset FROM vps WHERE id=(select max(id) from vps)",nativeQuery = true)
    public Integer findTimeIdMax();

    @Modifying
    @Transactional
    @Query(value = "DELETE  from vps where vps=?1",nativeQuery = true)
    public void deleteByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vps set vpsreset=1 where round((UNIX_TIMESTAMP()-timecheck/1000)/60)>=15",nativeQuery = true)
    public void resetVPSByTimecheck();
}
