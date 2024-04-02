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
    @Modifying
    @Transactional
    @Query(value = "UPDATE account_reg_tiktok SET running=0,vps='',proxy='',device_id='' where vps=?1",nativeQuery = true)
    public Integer resetAccountRegByVps(String vps);
    @Modifying
    @Transactional
    @Query(value = "update account_reg_tiktok set running=0,vps='',device_id='' where username not in (select username from account_tiktok) and round((UNIX_TIMESTAMP()-time_check/1000)/60)>=30;",nativeQuery = true)
    public Integer resetAccountRegTitkokThanTime();

}
