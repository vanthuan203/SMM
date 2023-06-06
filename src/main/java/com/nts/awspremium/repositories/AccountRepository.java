package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.VpsRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.Entity;
import javax.transaction.Transactional;
import java.security.PublicKey;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    @Query(value = "Select id from account where username=?1 limit 1",nativeQuery = true)
    public Long findIdUsername(String username);

    @Query(value = "Select vps,count(*) from account where vps in (select vps from vps where vpsoption=\"Buffh\") group by vps having count(*)>500  limit 20",nativeQuery = true)
    public List<String> getCountByVps();


    @Query(value = "Select id from account where username=?1 limit 1",nativeQuery = true)
    public Long findIdByUsername(String username);

    @Query(value = "Select proxy,proxy2 from account where id=?1 limit 1",nativeQuery = true)
    public String findProxyByIdSub(Long id);

    @Query(value = "Select proxy from account where id=?1 limit 1",nativeQuery = true)
    public String CheckProxyByIdSub(Long id);

    @Query(value = "Select count(*) from account where username=?1 and vps=?2 limit 1",nativeQuery = true)
    public Integer findUsernameByVps(String username,String vps);
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO account(username,password,recover,live,encodefinger,cookie,endtrial,endtrialstring,running,vps) VALUES(?1,?2,?3,?4,?5,?6,?7,?8,0,'')",nativeQuery = true)
    public void insertAccount(String username,String password,String recover,Integer live,String encodefinger,String cookie,Long endtrial,String endtrialstring);

    @Modifying
    @Transactional
    @Query(value = "update account set vps='',running=0 where vps like ?1 and username not in(select username from history where vps like ?1 and running=1) limit ?2",nativeQuery = true)
    public void updatelistaccount(String vps,Integer limit);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO account(username,password,recover,live,encodefinger,cookie,endtrial,endtrialstring,running,vps,date,geo) VALUES(?1,?2,?3,?4,?5,?6,0,'',?7,?8,?9,?10)",nativeQuery = true)
    public void insertAccountView(String username,String password,String recover,Integer live,String encodefinger,String cookie,Integer running,String vps,String date,String geo);
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO account(username,password,recover,live,running,vps,date,timeupdateinfo) VALUES(?1,?2,?3,?4,0,'',?5,?6)",nativeQuery = true)
    public void insertAccountSub(String username,String password,String recover,Integer live,String date,Long timeupdateinfo);
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET password=?1,recover=?2,live=?3,encodefinger=?4,cookie=?5,endtrial=?6,endtrialstring=?7 where username=?8",nativeQuery = true)
    public void updateAccount(String password,String recover,Integer live,String encodefinger,String cookie,Long endtrial,String endtrialstring,String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET endtrial=?1,endtrialstring='0',running=1,timecheck=?2 where id=?3",nativeQuery = true)
    public void updateTaskSub(Long endtrial,Long timecheck,Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET endtrial=0 where vps=?1",nativeQuery = true)
    public void delTaskSubError(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET password=?1,recover=?2,live=?3,encodefinger=?4,cookie=?5,timeupdateinfo=?6,running=0 where id=?7",nativeQuery = true)
    public void updateAccountSub(String password,String recover,Integer live,String encodefinger,String cookie,Long timeupdateinfo,Long id);
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET password=?1,recover=?2,live=?3,vps=?4 where id=?5",nativeQuery = true)
    public void updateAllInfoAccSub(String password,String recover,Integer live,String vps,Long id);
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET password=?1,recover=?2,live=?3,encodefinger=?4,cookie=?5,running=0 where id=?6",nativeQuery = true)
    public void updateAccountView(String password,String recover,Integer live,String encodefinger,String cookie,Long id);
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET endtrial=0  where round((UNIX_TIMESTAMP()-timecheck/1000)/60)>=10 and endtrial=1",nativeQuery = true)
    public void updateThreadSubError();

    @Modifying
    @Transactional
    @Query(value = "Update account SET vps='',running=0,live=1 where round((UNIX_TIMESTAMP()-timecheck/1000)/60/60)>=8 and live=0",nativeQuery = true)
    public void updateAccSubDieToLiveByTimecheck();
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=1,vps=?2,timecheck=?3 where id=?4",nativeQuery = true)
    public void updateAccountGetByVPS(Integer running,String vps,Long timecheck ,Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET proxy=?1 where id=?2",nativeQuery = true)
    public void updateProxyAccount(String proxy,Long id);

    @Query(value = "Select * from account where username=?1 limit 1",nativeQuery = true)
    public List<Account> findAccountByUsername(String username);
    @Query(value = "Select * from account where id=?1 limit 1",nativeQuery = true)
    public List<Account> findAccountById(Long id);

    @Query(value = "Select id from account where proxy='' order by rand() limit ?1",nativeQuery = true)
    public List<Long> getAccountByLimit(Integer limit);

    @Query(value = "Select count(*) from account where id=?1 and running=0",nativeQuery = true)
    public Integer checkAccountById(Long id);
    @Query(value = "Select password,recover,oldpassword from account where id=?1 limit 1",nativeQuery = true)
    public String getInfo(Long id);
    @Query(value = "Select count(*) from account where id=?1 and live=1 limit 1",nativeQuery = true)
    public Integer getCookieAccSub(Long id);
    @Query(value = "Select count(*) from account where id=?1 and vps like ?2 limit 1",nativeQuery = true)
    public Integer checkAcountByVps(Long id,String vps);

    @Query(value = "Select count(*) from account where id=?1 and vps like ?2 limit 1",nativeQuery = true)
    public Integer checkIdByVps(Long id,String vps);

    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccount();

    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountBuffh();

    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=1 and geo=?1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountBuffh(String geo);
    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=1 and geo=?1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountView(String geo);

    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=1 and geo=?1 and INSTR(username,'@gmail.com')>0 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountBuffhGmail(String geo);

    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and  live=1 and round((UNIX_TIMESTAMP()-timeupdateinfo/1000)/60/60/24)>=7 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountSub();

    @Query(value = "SELECT id  FROM account where live=0 and running=0 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1  order by rand()  limit 1",nativeQuery = true)
    public Long getAccountNeedLogin();

    @Query(value = "SELECT id  FROM account where live=?1  and running=0 and round((UNIX_TIMESTAMP()-endtrial/1000)/60/60/24)>=7 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountSubNeedLogin(Integer live);

    @Query(value = "SELECT id  FROM account where live!=1 and live!=2 and live!=5 and running=0 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountSubByWhere();

    @Query(value = "SELECT id FROM account where vps=?1 and running=0 and live=1 order by rand() limit 1",nativeQuery = true)
    public Long getaccountByVps(String vps);

    @Query(value = "SELECT id FROM account where vps like ?1 and running=0 and live=1 order by rand() limit 1",nativeQuery = true)
    public Long getaccountBufhByVps(String vps);

    @Query(value = "SELECT id FROM account where vps=?1 and running=0 and  live=1 and round((UNIX_TIMESTAMP()-timeupdateinfo/1000)/60/60/24)>=7 order by rand() limit 1",nativeQuery = true)
    public Long getAccountSubByVps(String vps);

    @Query(value = "SELECT count(*) FROM account where live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1",nativeQuery = true)
    public Integer getCountGmails();

    @Query(value = "SELECT count(*) FROM account where  round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1",nativeQuery = true)
    public Integer getCountGmailsByEndtrial();

    @Query(value = "SELECT count(*) FROM account where live=1",nativeQuery = true)
    public Integer getCountGmailLiveView();

    @Query(value = "SELECT count(*) FROM account",nativeQuery = true)
    public Integer getCountGmailBuffh();

    @Query(value = "SELECT count(*) FROM account ",nativeQuery = true)
    public Integer getCountGmailsSub();
    @Query(value = "SELECT count(*) FROM account where live=1 ",nativeQuery = true)
    public Integer getCountGmailsSubLive();
    @Query(value = "SELECT count(*) FROM account where endtrialstring='1' ",nativeQuery = true)
    public Integer getCountGmailsFullSub24h();
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=0,vps='',proxy='',proxy2='' where vps=?1",nativeQuery = true)
    public Integer resetAccountByVps(String vps);
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=0,vps='',live=?1,proxy='',proxy2='' where id=?2",nativeQuery = true)
    public Integer resetAccountByUsername(Integer live,Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET proxy=?1,proxy2=?2 where id=?3",nativeQuery = true)
    public Integer updateProxyById(String proxy,String proxy2,Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET proxy=?1 where id=?2",nativeQuery = true)
    public Integer updateProxyById(String proxy,Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET endtrialstring=?1 where id=?2",nativeQuery = true)
    public Integer updateTaskSub24h(String done,Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=1,live=1 where id=?1",nativeQuery = true)
    public Integer updateAccSubWhileCookieUpdate(Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET cookie=?1,running=1,live=1 where username=?2",nativeQuery = true)
    public Integer updatecookie(String cookie,String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=0,live=1,vps='' where id=?1",nativeQuery = true)
    public Integer updatecookieloginlocal(Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET timecheck=?1,running=1 where id=?2",nativeQuery = true)
    public Integer updatetimecheck(Long timecheck,Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=0 where vps=?1",nativeQuery = true)
    public void updateRunningByVPs(String vps);
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET timecheck=?1,running=1 where id=?2",nativeQuery = true)
    public Integer updateTimecheckById(Long timecheck,Long id);
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET vps='',running=0 where vps=?1 and INSTR(?2,username)=0",nativeQuery = true)
    public Integer updateListAccount(String vps,String listacc);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET vps='',running=0 where round((UNIX_TIMESTAMP()-timecheck/1000)/60/60)>=6",nativeQuery = true)
    public Integer resetAccountSubByTimecheck();

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET vps='',running=0 where round((UNIX_TIMESTAMP()-timecheck/1000)/60/60)>=24 and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 and running=1",nativeQuery = true)
    public Integer resetAccountByTimecheck();

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET vps='',running=0,live=1 where round((UNIX_TIMESTAMP()-timecheck/1000)/60/60)>=8 and live=0",nativeQuery = true)
    public Integer resetLoginAccountSubByTimecheck();


    @Query(value = "SELECT vps,round((UNIX_TIMESTAMP()-max(timecheck)/1000)/60) as time,count(*) as total FROM account where endtrial=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getvpsrunning();

    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccByVps();

    @Query(value = "SELECT geo FROM account where username=?1 limit 1",nativeQuery = true)
    public String getGeoByUsername(String username);

    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account where live=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccLiveByVps();
    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account where endtrialstring='1' group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccTaskSubByVps();

}
