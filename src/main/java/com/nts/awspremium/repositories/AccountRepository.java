package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account,String> {
    @Query(value = "SELECT * FROM account where account_id=?1 limit 1",nativeQuery = true)
    public Account get_Account_By_Account_id(String account_id);

    @Query(value = "SELECT * FROM account where profile_id=?1 and running=1 and platform=?2  limit 1",nativeQuery = true)
    public Account get_Account_By_ProfileId_And_Platfrom(String profile_id,String platform);


    @Query(value = "SELECT * FROM account where profile_id=?1 and platform=?2 and running=1  limit 1",nativeQuery = true)
    public Account get_Account_Ddependent_By_ProfileId_And_Platfrom(String profile_id,String platform);

    @Query(value = "Select count(*) from account where  device_id=?1 and (select max_reg from setting_tiktok limit 1)>(Select count(*) as total from account where live=1 and device_id=?1)",nativeQuery = true)
    public Integer Check_Get_Account_By_DeviceId(String device_id);

    @Query(value = "call update_running_account(?1,?2,?3)",nativeQuery = true)
    public Account get_Account_By_DeviceId(String device_id,Long time_check,String code);

    @Query(value = "call update_running_account_youtube(?1,?2,?3,?4,?5)",nativeQuery = true)
    public Account get_Account_Youtube_By_ProfileId(String profile_id,String device_id,Long time_check,String code,String mode);
    @Query(value = "call update_running_account_gmail(?1,?2,?3,?4)",nativeQuery = true)
    public Account get_Account_Gmail_By_ProfileId(String profile_id,String device_id,Long time_check,String code);

    @Query(value = "call update_running_account_platform(?1,?2,?3,?4,?5)",nativeQuery = true)
    public Account get_Account_Platform_By_ProfileId(String profile_id,String device_id,Long time_check,String code,String platform);

    @Query(value = "Select count(*) from account where device_id=?1",nativeQuery = true)
    public Integer check_Count_By_DeviceId(String device_id);

    @Query(value = "SELECT count(*) FROM Data.account where round((UNIX_TIMESTAMP()-add_time/1000)/60/60/24)>=?1 and platform=?2 and account_id=?3 and mode='register'",nativeQuery = true)
    public Integer check_Account_Task_True(Integer day,String platform,String account_id);

    @Query(value = "Select count(*) from account where account_id=?1",nativeQuery = true)
    public Integer check_Count_By_AccountId(String account_id);

    @Query(value = "SELECT count(*) FROM Data.account where profile_id=?1 and platform=?2 and round((UNIX_TIMESTAMP()-get_time/1000)/60/60)<24 and live>1",nativeQuery = true)
    public Integer check_Count_AccountDie24H_By_ProfileId(String profile_id,String platform);

    @Query(value = "SELECT count(*) FROM Data.account where profile_id=?1 and platform=?2 and live=2 and round((UNIX_TIMESTAMP()-get_time/1000)/60/60)<24",nativeQuery = true)
    public Integer check_Count_Account_VeryPhone_By_ProfileId(String profile_id,String platform);

    @Query(value = "SELECT count(*) FROM Data.account where device_id=?1 and platform='youtube' and mode='register' and round((UNIX_TIMESTAMP()-add_time/1000)/60/60/24)<7",nativeQuery = true)
    public Integer check_Count_Gmail_Reg_Less7D_By_DeviceId(String device_id);

    @Query(value = "SELECT count(*) FROM Data.account where profile_id=?1 and platform=?2 and running=1 and live=1 and round((UNIX_TIMESTAMP()-add_time/1000)/60/60/24)>=7",nativeQuery = true)
    public Integer check_Account_AddTime_Than7D(String profile_id,String platform);

    @Modifying
    @Transactional
    @Query(value = "update account set running=0,profile_id='',device_id='' where running=1  and account_id not in(select account_id from account_profile)",nativeQuery = true)
    public Integer reset_Account_Error();

    @Modifying
    @Transactional
    @Query(value = "update account set running=0,profile_id='',device_id='' where device_id in(?1)",nativeQuery = true)
    public Integer reset_Account_By_ListDevice(List<String> device_id);
    @Modifying
    @Transactional
    @Query(value = "update account set running=0,profile_id='',device_id='' where running=1  and device_id=?1",nativeQuery = true)
    public Integer reset_Account_By_Device(String device_id);
}
