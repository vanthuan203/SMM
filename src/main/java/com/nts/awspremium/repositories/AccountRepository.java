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
    @Query(value = "Select count(*) from account where username=?1 limit 1",nativeQuery = true)
    public Integer findUsername(String username);
    @Query(value = "Select count(*) from account where username=?1 and vps=?2 limit 1",nativeQuery = true)
    public Integer findUsernameByVps(String username,String vps);
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO account(username,password,recover,live,encodefinger,cookie,endtrial,endtrialstring,running,vps) VALUES(?1,?2,?3,?4,?5,?6,?7,?8,0,'')",nativeQuery = true)
    public void insertAccount(String username,String password,String recover,Integer live,String encodefinger,String cookie,Long endtrial,String endtrialstring);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO account(username,password,recover,live,encodefinger,cookie,endtrial,endtrialstring,running,vps) VALUES(?1,?2,?3,?4,?5,?6,0,'',?7,?8)",nativeQuery = true)
    public void insertAccountSub(String username,String password,String recover,Integer live,String encodefinger,String cookie,Integer running,String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET password=?1,recover=?2,live=?3,encodefinger=?4,cookie=?5,endtrial=?6,endtrialstring=?7,vps='',running=0 where username=?8",nativeQuery = true)
    public void updateAccount(String password,String recover,Integer live,String encodefinger,String cookie,Long endtrial,String endtrialstring,String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET endtrial=?1,endtrialstring='0',running=1 where username=?2",nativeQuery = true)
    public void updateTaskSub(Long endtrial,String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET endtrial=0 where vps=?1",nativeQuery = true)
    public void delTaskSubError(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET password=?1,recover=?2,live=?3,encodefinger=?4,cookie=?5,running=0 where username=?6",nativeQuery = true)
    public void updateAccountSub(String password,String recover,Integer live,String encodefinger,String cookie,String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=1,vps=?2,timecheck=?3 where id=?4",nativeQuery = true)
    public void updateAccountGetByVPS(Integer running,String vps,Long timecheck ,Integer id);

    @Query(value = "Select * from account where username=?1 limit 1",nativeQuery = true)
    public List<Account> findAccountByUsername(String username);
    @Query(value = "Select * from account where id=?1 limit 1",nativeQuery = true)
    public List<Account> findAccountById(Long id);

    @Query(value = "Select count(*) from account where id=?1 and running=0",nativeQuery = true)
    public Integer checkAccountById(Long id);
    @Query(value = "Select password,recover from account where username=?1 limit 1",nativeQuery = true)
    public String getInfo(String username);
    @Query(value = "Select count(*) from account where username=?1 and live=1 limit 1",nativeQuery = true)
    public Integer getCookieAccSub(String username);
    @Query(value = "Select count(*) from account where username=?1 and vps=?2 limit 1",nativeQuery = true)
    public Integer checkAcountByVps(String username,String vps);
    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccount();

    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountSub();

    @Query(value = "SELECT id  FROM account where live=0 and running=0 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1  order by rand()  limit 1",nativeQuery = true)
    public Long getAccountNeedLogin();

    @Query(value = "SELECT id  FROM account where live=0 and running=0 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountSubNeedLogin();

    @Query(value = "SELECT id FROM account where vps=?1 and running=0 and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand() limit 1",nativeQuery = true)
    public Long getaccountByVps(String vps);

    @Query(value = "SELECT id FROM account where vps=?1 and running=0 and live=1 order by rand() limit 1",nativeQuery = true)
    public Long getAccountSubByVps(String vps);

    @Query(value = "SELECT id FROM account where vps=?1 and running=0 and live=0 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand() limit 1",nativeQuery = true)
    public Long getaccountByVpsbuffh(String vps);
    @Query(value = "SELECT count(*) FROM account where round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 and username=?1",nativeQuery = true)
    public Integer checkEndTrial(String username);

    @Query(value = "SELECT count(*) FROM account where live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1",nativeQuery = true)
    public Integer getCountGmails();
    @Query(value = "SELECT count(*) FROM account ",nativeQuery = true)
    public Integer getCountGmailsSub();
    @Query(value = "SELECT count(*) FROM account where live=1 ",nativeQuery = true)
    public Integer getCountGmailsSubLive();
    @Query(value = "SELECT count(*) FROM account where endtrialstring='1' ",nativeQuery = true)
    public Integer getCountGmailsFullSub24h();
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=0,vps='' where vps like ?1",nativeQuery = true)
    public Integer resetAccountByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET endtrialstring=?1 where username=?2",nativeQuery = true)
    public Integer updateTaskSub24h(String done,String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=1,live=1 where username=?1",nativeQuery = true)
    public Integer updateAccSubWhileCookieUpdate(String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET cookie=?1,running=1,live=1 where username=?2",nativeQuery = true)
    public Integer updatecookie(String cookie,String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET timecheck=?1,running=1 where username=?2",nativeQuery = true)
    public Integer updatetimecheck(Long timecheck,String username);
    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET vps='',running=0 where vps=?1 and INSTR(?2,username)=0",nativeQuery = true)
    public Integer updateListAccount(String vps,String listacc);

    @Query(value = "SELECT vps,round((UNIX_TIMESTAMP()-max(timecheck)/1000)/60) as time,count(*) as total FROM account where endtrial=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getvpsrunning();

    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccByVps();

    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account where live=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccLiveByVps();
    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account where endtrialstring='1' group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccTaskSubByVps();

}
