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

}
