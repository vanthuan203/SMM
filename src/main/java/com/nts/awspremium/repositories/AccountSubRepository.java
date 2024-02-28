package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.AccountSub;
import com.nts.awspremium.model.VpsRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountSubRepository extends JpaRepository<AccountSub,Long> {
    @Query(value = "Select id from accountsub where username=?1 limit 1",nativeQuery = true)
    public Long findIdUsername(String username);

    @Query(value = "Select id from accountsub where username=?1 limit 1",nativeQuery = true)
    public Long findIdByUsername(String username);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO accountsub(username,password,recover,live,running,vps,date,timeupdateinfo) VALUES(?1,?2,?3,?4,?5,?6,?7,?8)",nativeQuery = true)
    public void insertAccountSub(String username,String password,String recover,Integer live,Integer running,String vps,String date,Long timeupdateinfo);

    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET endtrial=?1,endtrialstring='0',running=1,timecheck=?2 where id=?3",nativeQuery = true)
    public void updateTaskSub(Long endtrial,Long timecheck,Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET endtrial=0 where vps=?1",nativeQuery = true)
    public void delTaskSubError(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET password=?1,recover=?2,live=?3,vps=?4,running=?5,timeupdateinfo=?6 where id=?7",nativeQuery = true)
    public void updateAccountSub(String password,String recover,Integer live,String vps,Integer running,Long timeupdateinfo,Long id);
    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET endtrial=0  where round((UNIX_TIMESTAMP()-timecheck/1000)/60)>=10 and endtrial=1",nativeQuery = true)
    public void updateThreadSubError();

    @Modifying
    @Transactional
    @Query(value = "Update accountsub SET vps='',running=0,live=1 where round((UNIX_TIMESTAMP()-timecheck/1000)/60/60)>=8 and live=0",nativeQuery = true)
    public void updateAccSubDieToLiveByTimecheck();

    @Query(value = "Select * from accountsub where id=?1 limit 1",nativeQuery = true)
    public List<AccountSub> findAccountById(Long id);

    @Query(value = "Select count(*) from accountsub where id=?1 and running=0",nativeQuery = true)
    public Integer checkAccountById(Long id);
    @Query(value = "Select password,recover,oldpassword from accountsub where id=?1 limit 1",nativeQuery = true)
    public String getInfo(Long id);

    @Query(value = "Select count(*) from accountsub where id=?1 and vps like ?2 limit 1",nativeQuery = true)
    public Integer checkAcountByVps(Long id,String vps);

    @Query(value = "SELECT id  FROM accountsub where (vps is null or vps='' or vps=' ') and running=0 and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccount();

    @Query(value = "SELECT id  FROM accountsub where (vps is null or vps='' or vps=' ') and running=0 and  live=1  order by rand()  limit 1",nativeQuery = true)
    public Long getAccountSub();


    @Query(value = "SELECT id  FROM accountsub where live=?1  and running=0 and round((UNIX_TIMESTAMP()-endtrial/1000)/60/60/24)>=7 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountSubNeedLogin(Integer live);

    @Query(value = "SELECT id  FROM accountsub where live!=1 and live!=2 and live!=5 and running=0 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountSubByWhere();

    @Query(value = "SELECT id FROM accountsub where vps=?1 and running=0 and  live=1  order by rand() limit 1",nativeQuery = true)
    public Long getAccountSubByVps(String vps);

    @Query(value = "SELECT count(*) FROM accountsub",nativeQuery = true)
    public Integer getCountGmailBuffh();

    @Query(value = "SELECT count(*) FROM accountsub ",nativeQuery = true)
    public Integer getCountGmailsSub();
    @Query(value = "SELECT count(*) FROM accountsub where live=1 ",nativeQuery = true)
    public Integer getCountGmailsSubLive();
    @Query(value = "SELECT count(*) FROM accountsub where endtrialstring='1' ",nativeQuery = true)
    public Integer getCountGmailsFullSub24h();
    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET running=0,vps='',proxy='',proxy2='' where vps=?1",nativeQuery = true)
    public Integer resetAccountByVps(String vps);
    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET running=0,vps='',live=?1,proxy='',proxy2='' where id=?2",nativeQuery = true)
    public Integer resetAccountByUsername(Integer live,Long id);





    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET endtrialstring=?1 where id=?2",nativeQuery = true)
    public Integer updateTaskSub24h(String done,Long id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET timecheck=?1,running=1 where id=?2",nativeQuery = true)
    public Integer updatetimecheck(Long timecheck,Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET running=0 where vps=?1",nativeQuery = true)
    public void updateRunningByVPs(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET vps='',running=0 where vps=?1 and INSTR(?2,username)=0",nativeQuery = true)
    public Integer updateListAccount(String vps,String listacc);

    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET vps='',running=0 where round((UNIX_TIMESTAMP()-timecheck/1000)/60/60)>=6",nativeQuery = true)
    public Integer resetAccountSubByTimecheck();

    @Modifying
    @Transactional
    @Query(value = "UPDATE accountsub SET vps='',running=0,live=1 where round((UNIX_TIMESTAMP()-timecheck/1000)/60/60)>=8 and live=0",nativeQuery = true)
    public Integer resetLoginAccountSubByTimecheck();


    @Query(value = "SELECT vps,round((UNIX_TIMESTAMP()-max(timecheck)/1000)/60) as time,count(*) as total FROM account where endtrial=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getvpsrunning();

    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM accountsub group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccByVps();

    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM accountsub where live=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccLiveByVps();
    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM accountsub where endtrialstring='1' group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccTaskSubByVps();

}
