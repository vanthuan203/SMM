package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Vps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VpsRepository extends JpaRepository<Vps,Integer> {
    @Query(value = "select * from vps order by CAST(SUBSTRING_INDEX(vps, '-', -1) AS UNSIGNED) ASC",nativeQuery = true)
    public List<Vps> getListVPS();
    @Query(value = "select * from vps order by CAST(SUBSTRING_INDEX(vps, '-', -1) AS UNSIGNED) ASC",nativeQuery = true)
    public List<Vps> getListVPSSub();

    @Query(value = "Select count(*) from vps where vps like ?1 and ((select count(*) from account where running=1 and vps like ?1))<threads*2",nativeQuery = true)
    public Integer checkGetAccountByThreadVps(String vps);
    @Query(value = "Select count(*) from vps where vps=?1 and ((select count(*) from account where running=1 and vps=?1))<threads*17",nativeQuery = true)
    public Integer checkGetAccount17ByThreadVps(String vps);

    @Query(value = "Select count(*) from vps where vps=?1 and ((select count(*) from account where running=1 and vps=?1))<threads*15",nativeQuery = true)
    public Integer checkGetAccount15ByThreadVps(String vps);

    @Query(value = "select * from vps where vps like ?1",nativeQuery = true)
    public List<Vps> findVPS(String vps);

    @Query(value = "select state from vps where vps like ?1",nativeQuery = true)
    public Integer getState(String vps);

    @Query(value = "SELECT * FROM vps where round((UNIX_TIMESTAMP()-timecheck/1000)/60) >=5 order by CAST(SUBSTRING(vps,position('-' in vps),length(vps)) AS UNSIGNED) desc;",nativeQuery = true)
    public List<Vps> findVPSDie();

    @Query(value = "SELECT timereset FROM vps WHERE id=(select max(id) from vps)",nativeQuery = true)
    public Integer findTimeIdMax();
    @Query(value = "SELECT threads FROM vps WHERE vps=?1",nativeQuery = true)
    public Integer getThreadVPS(String vps);

    @Modifying
    @Transactional
    @Query(value = "DELETE  from vps where vps=?1",nativeQuery = true)
    public void deleteByVps(String vps);



    @Modifying
    @Transactional
    @Query(value = "UPDATE vps set vpsreset=1 where round((UNIX_TIMESTAMP()-timecheck/1000)/60)>=15 and round((UNIX_TIMESTAMP()-timecheck/1000)/60)<30 ",nativeQuery = true)
    public void resetVPSByTimecheck();

    @Modifying
    @Transactional
    @Query(value = "UPDATE vps set vpsreset=1",nativeQuery = true)
    public void resetBasByCron();

    @Modifying
    @Transactional
    @Query(value = "UPDATE vps set vpsreset=2 where round((UNIX_TIMESTAMP()-timecheck/1000)/60)>=30",nativeQuery = true)
    public void resetVPSAndUnrarToolByTimecheck();
}
