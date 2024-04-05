package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.AccountTiktok;
import com.nts.awspremium.model.DeviceRunning;
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

    @Query(value = "Select proxy from account_tiktok where username=?1 limit 1",nativeQuery = true)
    public String getProxyByUsername(String username);

    @Query(value = "Select vps,count(*) from account_tiktok group by vps",nativeQuery = true)
    public List<String> countbyVPS();

    @Query(value = "Select count(*) from account_tiktok where device_id=?1 and (select max_reg from setting_tiktok limit 1)>(Select count(*) as total from account_tiktok where device_id=?1)",nativeQuery = true)
    public Integer CheckRegByDeviceId(String device_id);
    @Query(value = "Select count(*) from account_tiktok where device_id=?1",nativeQuery = true)
    public Integer getCountByDeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account_tiktok SET running=0,vps='',proxy='',device_id='' where vps=?1",nativeQuery = true)
    public Integer resetAccountByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account_tikok SET running=0 where vps=?1",nativeQuery = true)
    public void updateRunningByVPs(String vps);


    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account_tiktok group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccByVps();

    @Query(value = "SELECT device_id,vps,count(*) as total FROM account_tiktok where vps=?1 group by device_id order by total desc",nativeQuery = true)
    public List<DeviceRunning> getCountAccByDeviceByVps(String vps);

    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account_tiktok where live=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccLiveByVps();


}
