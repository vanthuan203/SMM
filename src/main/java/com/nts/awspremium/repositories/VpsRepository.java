package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Vps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VpsRepository extends JpaRepository<Vps,Integer> {
    @Query(value = "select * from vps where !(vpsoption='Sub_Pending' or vpsoption='sub') order by CAST(SUBSTRING_INDEX(vps, '-', -1) AS UNSIGNED) ASC",nativeQuery = true)
    public List<Vps> getListVPS();
    @Query(value = "select * from vps where (vpsoption='Sub_Pending' or vpsoption='sub') order by CAST(SUBSTRING_INDEX(vps, '-', -1) AS UNSIGNED) ASC",nativeQuery = true)
    public List<Vps> getListVPSSub();

    @Query(value = "Select count(*) from vps where vps like ?1 and ((select count(*) from account where running=1 and vps like ?1))<threads*2",nativeQuery = true)
    public Integer checkGetAccountByThreadVps(String vps);
    @Query(value = "Select count(*) from vps where vps=?1 and ((select count(*) from account where running=1 and vps=?1))<threads*17",nativeQuery = true)
    public Integer checkGetAccount17ByThreadVps(String vps);

    @Query(value = "Select cmt from vps where vps=?1 limit 1",nativeQuery = true)
    public Integer checkVpsCmtTrue(String vps);

    @Query(value = "Select get_account from vps where vps=?1 limit 1",nativeQuery = true)
    public Integer checkVpsGetAccountTrue(String vps);

    @Query(value = "Select count(*) from vps where vps=?1 and ((select count(*) from account where geo=?2 and running=1 and vps=?1))<threads*(select leveluser from setting where id=1)",nativeQuery = true)
    public Integer checkGetAccount5ByThreadVps(String vps,String geo);

    @Query(value = "Select count(*) from vps where vps=?1 and ((select count(*) from account where geo=?2 and running=1 and vps=?1))<(select cmtcountuser from setting where id=1)",nativeQuery = true)
    public Integer checkGetAccountCmtByVps(String vps,String geo);

    @Query(value = "Select count(*) from vps where vps=?1",nativeQuery = true)
    public Integer checkVpsValid(String vps);

    @Query(value = "select * from vps where vps=?1",nativeQuery = true)
    public List<Vps> findVPS(String vps);

    @Query(value = "select count(*) from vps where live=1 and vps=?1",nativeQuery = true)
    public Integer CheckVPSLiveTrue(String vps);

    @Query(value = "select state from vps where vps like ?1",nativeQuery = true)
    public Integer getState(String vps);

    @Query(value = "SELECT * FROM vps where round((UNIX_TIMESTAMP()-timecheck/1000)/60) >=5 order by CAST(SUBSTRING(vps,position('-' in vps),length(vps)) AS UNSIGNED) desc;",nativeQuery = true)
    public List<Vps> findVPSDie();

    @Query(value = "SELECT timereset FROM vps WHERE id=(select max(id) from vps)",nativeQuery = true)
    public Integer findTimeIdMax();
    @Query(value = "SELECT sum(threads) FROM AccPremium.vps where vpsoption='live'",nativeQuery = true)
    public Integer getSumThreadLive();

    @Query(value = "SELECT threads FROM vps WHERE vps=?1",nativeQuery = true)
    public Integer getThreadVPS(String vps);

    @Query(value = "SELECT sum(threads) FROM vps WHERE vpsoption=?1",nativeQuery = true)
    public Integer getSumThreadsByGeo(String vpsoption);

    @Query(value = "select count(*) from vps where round((UNIX_TIMESTAMP()-timecheck/1000)/60)<15 and timecheck>timeresettool  and timereset!=DATE_FORMAT(ADDDATE( UTC_TIMESTAMP(), INTERVAL +7 HOUR), '%d') and dayreset=DATE_FORMAT(ADDDATE( UTC_TIMESTAMP(), INTERVAL +7 HOUR), '%d')",nativeQuery = true)
    public Integer checkResetVPSNext();

    @Modifying
    @Transactional
    @Query(value = "DELETE  from vps where vps=?1",nativeQuery = true)
    public void deleteByVps(String vps);



    @Modifying
    @Transactional
    @Query(value = "UPDATE vps set vpsreset=1,timeresettool=?1 where round((UNIX_TIMESTAMP()-timecheck/1000)/60)>=15 and round((UNIX_TIMESTAMP()-timecheck/1000)/60)<30 ",nativeQuery = true)
    public void resetVPSByTimecheck(Long timeresettool);

    @Modifying
    @Transactional
    @Query(value = "update vps set vpsreset=1,timeresettool=?1 where vps not in (select vps from historyview where round((UNIX_TIMESTAMP()-timeget/1000)/60)<20 group by vps ) and vpsoption!='Pending' and vpsreset=0 limit 15",nativeQuery = true)
    public void resetVPSByHisTimecheck(Long timeresettool);

    @Query(value = "Select count(*) from vps where vps=?1 and ((select count(*) from account where running=1 and vps=?1))<threads",nativeQuery = true)
    public Integer checkGetAccountAccSub(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vps set vpsreset=2,get_account=1,cmt=1,proxy=1,timeresettool=?1,dayreset=DATE_FORMAT(ADDDATE( UTC_TIMESTAMP(), INTERVAL +7 HOUR), '%d') where timeresettool=0 order by rand() limit ?2",nativeQuery = true)
    public void resetBasByCron(Long timeresettool,Integer limit);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vps set timeresettool=0",nativeQuery = true)
    public void resetTimeResetTool();

    @Modifying
    @Transactional
    @Query(value = "update vps set get_account=0 where vpsoption=?1",nativeQuery = true)
    public Integer resetGetAcountAll(String geo);
    @Query(value = "call changer_account_vn(?1)",nativeQuery = true)
    public Integer changer_account_vn(String geo);
    @Query(value = "call changer_account_us(?1)",nativeQuery = true)
    public Integer changer_account_us(String geo);
    @Modifying
    @Transactional
    @Query(value = "UPDATE vps set vpsreset=1,timeresettool=?1 where vps=?2",nativeQuery = true)
    public void updateRestartVpsByName(Long timeresettool,String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vps set vpsreset=2 where round((UNIX_TIMESTAMP()-timecheck/1000)/60)>=30",nativeQuery = true)
    public void resetVPSAndUnrarToolByTimecheck();
}
