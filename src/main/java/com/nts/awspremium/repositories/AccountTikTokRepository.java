package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.AccountTiktok;
import com.nts.awspremium.model.VpsRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountTikTokRepository extends JpaRepository<AccountTiktok,String> {
    @Query(value = "Select * from account_tiktok where username=?1 limit 1",nativeQuery = true)
    public AccountTiktok checkUsername(String username);
    @Query(value = "Select * from account_tiktok where username=?1 limit 1",nativeQuery = true)
    public AccountTiktok findAccountByUsername(String username);

    @Query(value = "Select count(*) from account_tiktok where username=?1 limit 1",nativeQuery = true)
    public Integer findIdByUsername(String username);

    @Query(value = "Select count(*) from account_tiktok where device_id=?1 and (select max_reg from setting_tiktok limit 1)>(Select count(*) as total from account_tiktok where device_id=?1)",nativeQuery = true)
    public Integer CheckRegByDeviceId(String device_id);
    @Query(value = "Select count(*) from account_tiktok where device_id=?1",nativeQuery = true)
    public Integer getCountByDeviceId(String device_id);

    @Query(value = "Select proxy,proxy2 from account where id=?1 limit 1",nativeQuery = true)
    public String findProxyByIdSub(Long id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO account(username,password,recover,live,encodefinger,cookie,endtrial,endtrialstring,running,vps,date,geo) VALUES(?1,?2,?3,?4,?5,?6,0,'',?7,?8,?9,?10)",nativeQuery = true)
    public void insertAccountView(String username,String password,String recover,Integer live,String encodefinger,String cookie,Integer running,String vps,String date,String geo);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account_tiktok SET password=?1,recover=?2,live=?3,encodefinger=?4,cookie=?5,running=0 where id=?6",nativeQuery = true)
    public void updateAccountView(String password,String recover,Integer live,String encodefinger,String cookie,Long id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET proxy=?1 where id=?2",nativeQuery = true)
    public void updateProxyAccount(String proxy,Long id);

    @Query(value = "Select * from account where id=?1 limit 1",nativeQuery = true)
    public List<Account> findAccountById(Long id);

    @Query(value = "Select id from account where proxy='' order by rand() limit ?1",nativeQuery = true)
    public List<Long> getAccountByLimit(Integer limit);

    @Query(value = "Select count(*) from account where id=?1 and running=0",nativeQuery = true)
    public Integer checkAccountById(Long id);
    @Query(value = "Select password,recover,oldpassword from account where id=?1 limit 1",nativeQuery = true)
    public String getInfo(Long id);


    @Query(value = "Select count(*) from account where id=?1 and vps like ?2 limit 1",nativeQuery = true)
    public Integer checkIdByVps(Long id,String vps);


    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccount();




    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=1 and geo=?1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountView(String geo);

    @Query(value = "SELECT id  FROM account where live=0 and running=0 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1  order by rand()  limit 1",nativeQuery = true)
    public Long getAccountNeedLogin();

    @Query(value = "SELECT id FROM account where vps=?1 and running=0 and live=1 and geo=?2 order by rand() limit 1",nativeQuery = true)
    public Long getaccountByVps(String vps,String geo);


    @Query(value = "SELECT proxy FROM account where username=?1 limit 1",nativeQuery = true)
    public String getProxyByUsername(String username);


    @Query(value = "SELECT count(*) FROM account where live=1",nativeQuery = true)
    public Integer getCountGmailLiveView();

    @Query(value = "SELECT count(*) FROM account",nativeQuery = true)
    public Integer getCountGmailBuffh();

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=0,vps='',proxy='',proxy2='' where vps=?1",nativeQuery = true)
    public Integer resetAccountByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=0,vps='',proxy='',proxy2='' where geo not like 'cmt%' and vps=?1",nativeQuery = true)
    public Integer resetAccountViewByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=0,vps='',proxy='',proxy2='' where geo like 'cmt%' and vps=?1",nativeQuery = true)
    public Integer resetAccountCmtByVps(String vps);
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
    @Query(value = "UPDATE account SET running=0 where vps=?1",nativeQuery = true)
    public void updateRunningByVPs(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=0 where vps=?1 and geo like 'cmt%'",nativeQuery = true)
    public void updateRunningAccCmtByVPs(String vps);
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
    @Query(value = "UPDATE account SET vps='',running=0 where round((UNIX_TIMESTAMP()-timecheck/1000)/60/60)>=24 and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 and running=1",nativeQuery = true)
    public Integer resetAccountByTimecheck();

    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccByVps();

    @Query(value = "SELECT geo FROM account where username=?1 limit 1",nativeQuery = true)
    public String getGeoByUsername(String username);

}
