package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.AccountRegTiktok;
import com.nts.awspremium.model.AccountTiktok;
import com.nts.awspremium.model.VpsRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountRegTikTokRepository extends JpaRepository<AccountRegTiktok,String> {

    @Query(value = "Select * from account_reg_tiktok where username=?1 limit 1",nativeQuery = true)
    public AccountRegTiktok checkUsername(String username);
    @Query(value = "call update_running_acc_reg_tiktok(?1,?2,?3,?4)",nativeQuery = true)
    public AccountRegTiktok getAccountRegTiktok(String vps,String device_id,Long time_check,String code);

    @Query(value = "call update_running_acc_reg_tiktok_by_type(?1,?2,?3,?4,?5)",nativeQuery = true)
    public AccountRegTiktok getAccountRegTiktokByAccountType(String vps,String device_id,Long time_check,String code,String account_type);
    @Modifying
    @Transactional
    @Query(value = "UPDATE account_reg_tiktok SET running=0,vps='',proxy='',device_id='' where vps=?1",nativeQuery = true)
    public Integer resetAccountRegByVps(String vps);

    @Query(value = "Select proxy from account_reg_tiktok where username=?1 limit 1",nativeQuery = true)
    public String getProxyByUsernameReg(String username);

    @Query(value = "Select account_type from account_reg_tiktok where username=?1 limit 1",nativeQuery = true)
    public String getAccountTypeByUsernameReg(String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account_reg_tiktok SET vps=?1 where device_id=?2",nativeQuery = true)
    public void updateVPSByDevice(String vps,String device_id);

    @Query(value = "Select username from account_reg_tiktok  where vps=?1",nativeQuery = true)
    public List<String> getUsernameRegByVps(String vps);

    @Query(value = "select username from account_reg_tiktok  where running=1 and  username not in (select username from account_tiktok) and round((UNIX_TIMESTAMP()-time_check/1000)/60)>=30;",nativeQuery = true)
    public List<String> getUsernameRegTitkokThanTime();

}
