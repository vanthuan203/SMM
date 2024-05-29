package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.AccountTask;
import com.nts.awspremium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account,String> {
    @Query(value = "Select count(*) from account where  device_id=?1 and (select max_reg from setting_tiktok limit 1)>(Select count(*) as total from account where live=1 and device_id=?1)",nativeQuery = true)
    public Integer Check_Get_Account_By_DeviceId(String device_id);

    @Query(value = "call update_running_account(?1,?2,?3)",nativeQuery = true)
    public Account get_Account_By_DeviceId(String device_id,Long time_check,String code);

    @Query(value = "call update_running_account(?1,?2,?3,?4)",nativeQuery = true)
    public Account get_Account_By_ProfileId(String profile_id,String device_id,Long time_check,String code);

    @Query(value = "Select count(*) from account where device_id=?1",nativeQuery = true)
    public Integer check_Count_By_DeviceId(String device_id);
}
