package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountReg;
import com.nts.awspremium.model.AccountTiktok;
import com.nts.awspremium.model.DeviceRunning;
import com.nts.awspremium.model.VpsRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountRegRepository extends JpaRepository<AccountReg,String> {
    @Query(value = "Select * from account_reg where username=?1 limit 1",nativeQuery = true)
    public AccountReg checkUsername(String username);
    @Query(value = "Select * from account_reg where username=?1 limit 1",nativeQuery = true)
    public AccountReg findAccountByUsername(String username);

    @Query(value = "Select count(*) from account_reg where username=?1 limit 1",nativeQuery = true)
    public Integer findIdByUsername(String username);

    @Query(value = "Select proxy from account_reg where username=?1 limit 1",nativeQuery = true)
    public String getProxyByUsername(String username);

    @Query(value = "Select vps,count(*) from account_reg group by vps",nativeQuery = true)
    public List<String> countbyVPS();

    @Query(value = "Select count(*) from account_reg where device_id=?1 and WEEKOFYEAR(FROM_UNIXTIME((time_add/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d'))=WEEKOFYEAR(DATE_SUB(DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d'),INTERVAL 1 DAY))",nativeQuery = true)
    public Integer CheckRegByDeviceId(String device_id);
    @Query(value = "Select count(*) from account_reg where device_id=?1",nativeQuery = true)
    public Integer getCountByDeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "Delete from account_reg  where vps=?1",nativeQuery = true)
    public Integer deleteAccountTiktokByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account_reg SET running=0 where vps=?1",nativeQuery = true)
    public void updateRunningByVPs(String vps);


    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account_reg group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccByVps();

    @Query(value = "SELECT device_id,vps,count(*) as total,max(time_add) as time_add FROM account_reg where vps='9003' group by device_id order by total desc",nativeQuery = true)
    public List<DeviceRunning> getCountAccByDeviceByVps(String vps);

    @Query(value = "SELECT vps,round(0) as time,count(*) as total FROM account_tiktok where live=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getCountAccLiveByVps();


}
