package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.AccountProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountProfileRepository extends JpaRepository<AccountProfile,String> {
    @Query(value = "SELECT * FROM account where account_id=?1 limit 1",nativeQuery = true)
    public Account get_Account_By_Account_id(String account_id);
    @Query(value = "Select count(*) from account where  device_id=?1 and (select max_reg from setting_tiktok limit 1)>(Select count(*) as total from account where live=1 and device_id=?1)",nativeQuery = true)
    public Integer Check_Get_Account_By_DeviceId(String device_id);

    @Query(value = "call update_running_account(?1,?2,?3)",nativeQuery = true)
    public Account get_Account_By_DeviceId(String device_id,Long time_check,String code);

    @Query(value = "call update_running_account(?1,?2,?3,?4)",nativeQuery = true)
    public Account get_Account_By_ProfileId(String profile_id,String device_id,Long time_check,String code);

    @Query(value = "Select count(*) from account where device_id=?1",nativeQuery = true)
    public Integer check_Count_By_DeviceId(String device_id);
}
