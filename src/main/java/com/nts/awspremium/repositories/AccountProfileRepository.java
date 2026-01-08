package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.AccountProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountProfileRepository extends JpaRepository<AccountProfile,String> {
    @Query(value = "SELECT SUBSTRING_INDEX(account_id, '|', 1) as account_id FROM account_profile where profile_id=?1 and platform=?2 and live=1 limit 1",nativeQuery = true)
    public String get_AccountId_Like_By_AccountId_And_Platform(String profile_id,String platform);

    @Query(value = "SELECT name FROM account_profile where account_id=?1 limit 1",nativeQuery = true)
    public String get_Name_By_AccountId(String account_id);

    @Query(value = "SELECT recover FROM account_profile where account_id=?1 limit 1",nativeQuery = true)
    public String get_Recover_By_AccountId(String account_id);

    @Query(value = "SELECT avatar FROM account_profile where account_id=?1 limit 1",nativeQuery = true)
    public Integer get_Avatar_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM account_profile where platform='tiktok'",nativeQuery = true)
    public List<AccountProfile> get_Account_Tiktok();

    @Query(value = "SELECT account_id FROM account_profile where profile_id=?1 and platform=?2  limit 1",nativeQuery = true)
    public String get_AccountId_By_AccountId_And_Platform(String profile_id,String platform);

    @Query(value = "SELECT account_id FROM account_profile where profile_id=?1 and platform=?2  limit 1",nativeQuery = true)
    public String get_Account_By_AccountId_And_Platform(String profile_id,String platform);

    @Query(value = "SELECT account_id FROM account_profile where profile_id=?1 and platform=?2 order by task_time asc  limit 1",nativeQuery = true)
    public String get_AccountId_By_Platform_And_ProfileId(String profile_id,String platform);

    @Query(value = "SELECT * FROM account_profile where profile_id=?1 and platform=?2 and live>=0 order by running desc,task_time asc  limit 1",nativeQuery = true)
    public AccountProfile get_Account_By_Platform_And_ProfileId(String profile_id,String platform);

    @Query(value = "SELECT count(*) FROM account_profile where profile_id=?1 and platform=?2  and live=1 limit 1",nativeQuery = true)//and account_id not like '%@gmail%'
    public Integer check_AccountLive_By_ProfileId_And_Platform(String profile_id,String platform);

    @Query(value = "SELECT count(*) FROM account_profile where profile_id=?1 and platform=?2 and account_id=?3 and live=1 limit 1",nativeQuery = true)
    public Integer check_AccountLive_By_ProfileId_And_Platform_And_AccountId(String profile_id,String platform,String account_id);


    @Query(value = "SELECT count(*) FROM account_profile where profile_id=?1 and platform=?2 ",nativeQuery = true)//and account_id not like '%@gmail%'
    public Integer check_Count_Account_By_ProfileId_And_Platform(String profile_id,String platform);

    @Query(value = "SELECT count(*) FROM account_profile where profile_id=?1 and platform=?2  and live>=0",nativeQuery = true) //and account_id not like '%@gmail%'
    public Integer check_Count_AccountLive_By_ProfileId_And_Platform(String profile_id,String platform);

    @Query(value = "SELECT count(*) FROM account_profile where profile_id=?1 and platform=?2  and live=1",nativeQuery = true) //and account_id not like '%@gmail%'
    public Integer check_Count_AccountLive1_By_ProfileId_And_Platform(String profile_id,String platform);

    @Query(value = "SELECT count(*) FROM account_profile where profile_id=?1 and platform=?2  and live=1 limit 1",nativeQuery = true)//and account_id like '%@gmail%'
    public Integer check_AccountLive_Gmail_By_ProfileId_And_Platform(String profile_id,String platform);
    @Query(value = "SELECT count(*) FROM account_profile where account_id=?1 limit 1",nativeQuery = true)
    public Integer check_Account_By_AccountId(String account_id);
    @Query(value = "SELECT * FROM account_profile where profile_id=?1 and platform=?2  limit 1",nativeQuery = true) //and account_id not like '%@gmail%'
    public AccountProfile get_Account_By_ProfileId_And_Platform(String profile_id,String platform);

    @Query(value = "SELECT * FROM account_profile where profile_id=?1 and platform=?2 and code=?3  limit 1",nativeQuery = true) //and account_id not like '%@gmail%'
    public AccountProfile get_Account_By_ProfileId_And_Platform_And_Code(String profile_id,String platform,String code);

    @Query(value = "SELECT * FROM account_profile where profile_id=?1 and platform=?2 and connection_platform not like ?3  and live=1 limit 1",nativeQuery = true) //and account_id not like '%@gmail%'
    public AccountProfile get_Account_DependentLive_By_ProfileId_And_Platform(String profile_id,String platform,String platform_connection);

    @Query(value = "SELECT count(*) FROM account_profile where profile_id=?1 and platform=?2 and connection_platform not like ?3  and live=1 limit 1",nativeQuery = true) //and account_id not like '%@gmail%'
    public Integer get_Count_Account_DependentLive_By_ProfileId_And_Platform(String profile_id,String platform,String platform_connection);

    @Query(value = "SELECT * FROM account_profile where profile_id=?1 and platform=?2  and live!=1 order by add_time asc limit 1",nativeQuery = true) //and account_id not like '%@gmail%'
    public AccountProfile get_AccountLive0_By_ProfileId_And_Platform(String profile_id,String platform);

    @Query(value = "SELECT * FROM account_profile where profile_id=?1 and platform=?2 limit 1",nativeQuery = true)
    public AccountProfile get_AccountLike_By_ProfileId_And_Platform(String profile_id,String platform);

    @Query(value = "SELECT SUBSTRING_INDEX(account_id, '|', 1) as account_id FROM account_profile where profile_id=?1 and platform=?2 limit 1",nativeQuery = true)
    public String get_AccountId_Live_By_AccountId_And_Platform(String profile_id,String platform);
    @Query(value = "Select count(*) from account where  device_id=?1 and (select max_reg from setting_tiktok limit 1)>(Select count(*) as total from account where live=1 and device_id=?1)",nativeQuery = true)
    public Integer Check_Get_Account_By_DeviceId(String device_id);

    @Query(value = "call update_running_account(?1,?2,?3)",nativeQuery = true)
    public Account get_Account_By_DeviceId(String device_id,Long time_check,String code);

    @Query(value = "call update_running_account(?1,?2,?3,?4)",nativeQuery = true)
    public Account get_Account_By_ProfileId(String profile_id,String device_id,Long time_check,String code);

    @Query(value = "Select * from account_profile where account_id=?1 and platform=?2 limit 1",nativeQuery = true)
    public AccountProfile get_Account_By_Account_id_And_Platform(String account_id,String platform);

    @Query(value = "select count(*) from account_profile where platform=?1 and profile_id like ?2 and round((UNIX_TIMESTAMP()-login_time/1000)/60/60)<?3",nativeQuery = true)
    public Integer count_Login_By_Platform_And_DeviceId(String platform,String device_id,Integer count);

    @Query(value = "select count(*) from account_profile where platform=?1 and profile_id like ?2 and login_time=0",nativeQuery = true)
    public Integer count_Login_Time_Null_By_Platform_And_DeviceId(String platform,String device_id);

    @Query(value = "SELECT count(*) FROM Data.account_profile where round((UNIX_TIMESTAMP()-login_time/1000)/60/60/24)>=?1 and platform=?2 and account_id=?3",nativeQuery = true)
    public Integer check_Account_Task_True(Integer day,String platform,String account_id);
    @Query(value = "SELECT count(*) FROM Data.account_profile where profile_id like ?1 and platform='tiktok' and  account_id not LIKE '@%' ",nativeQuery = true)
    public Integer count_Reg_Tiktok_DeviceId(String device_id);


    @Query(value = "select count(*) from account_profile where platform=?1 and profile_id like ?2 and live=-1",nativeQuery = true)
    public Integer count_Register_Task_By_Platform_And_DeviceId(String platform,String device_id);

    @Query(value = "select count(*) from account_profile where platform=?1 and profile_id=?2 and account_id like '%@gmail%' and round((UNIX_TIMESTAMP()-login_time/1000)/60/60/24)<?3",nativeQuery = true)
    public Integer count_Gmail_By_Platform_And_PrfoileId(String platform,String profile_id,Integer day);

    @Query(value = "Select * from account_profile where account_id=?1 limit 1",nativeQuery = true)
    public AccountProfile get_Account_By_Account_id(String account_id);

    @Query(value = "SELECT * FROM account_profile where profile_id=?1 and platform='youtube' and code=?2 limit 1",nativeQuery = true)
    public AccountProfile get_Account_Youtube_By_ProfileId_And_Code(String profile_id,String code);

    @Query(value = "SELECT COALESCE(GROUP_CONCAT(SUBSTRING_INDEX(account_id, '|youtube', 1)),'') FROM account_profile where profile_id=?1 and platform='youtube'",nativeQuery = true)
    public String get_List_Account_Youtube_By_ProfileId(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "delete from account_profile where round((UNIX_TIMESTAMP()-update_time/1000)/60/60)>24;",nativeQuery = true)
    public Integer deleteAllByThan24h();

    @Modifying
    @Transactional
    @Query(value = "update  account_profile set running=0,sign_in=1 where profile_id=?1",nativeQuery = true)
    public Integer update_Running_By_ProfileId(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "update  account_profile set live=0,last_time=0 where profile_id=?1 and platform='tiktok' and live=1",nativeQuery = true)
    public Integer update_Live_Tiktok_By_ProfileId(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "update  account_profile set sign_in=1 where profile_id=?1",nativeQuery = true)
    public Integer update_SignIn_By_ProfileId(String profile_id);


    @Query(value = "select count(*) from account_profile where profile_id=?1 and platform!='youtube' and live>=0",nativeQuery = true)
    public Integer get_Account_Tiktok_By_ProfileId(String profile_id);

}
