package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ProfileShow;
import com.nts.awspremium.model.ProfileTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import javax.transaction.Transactional;
import java.util.List;

public interface ProfileTaskRepository extends JpaRepository<ProfileTask,String> {

    @Query(value = "Select * from profile_task where profile_id=?1 limit 1",nativeQuery = true)
    public ProfileTask check_ProfileId(String profile_id);

    @Query(value = "select count(*) from profile_task where enabled=0 and  device_id=?1",nativeQuery = true)
    public Integer count_Profile_Not_Enable(String device_id);

    @Query(value = "SELECT count(*) FROM Data.profile_task where running=1 and order_id in(select order_id from order_running where service_id in (SELECT service_id FROM service where mode='auto' and task='follower'));",nativeQuery = true)
    public Integer count_Profile_Running_And_Mode_Auto();

    @Query(value = "select count(*) from profile_task where device_id=?1",nativeQuery = true)
    public Integer count_Profile(String device_id);

    @Query(value = "Select * from profile_task where  device_id=?1 and round((UNIX_TIMESTAMP()-(select max(google_time) from profile_task where\n" +
            "  device_id=?1)/1000)/60/60)>=(select time_enable_profile from mode where mode=?2) and enabled=0 order by rand() limit 1",nativeQuery = true)
    public ProfileTask get_Profile_Rand_Enable0(String device_id,String mode);

    @Query(value = "Select * from profile_task where profile_id!=?1 and device_id=?2 and round((UNIX_TIMESTAMP()-(select max(enabled_time) from profile_task where  device_id=?2)/1000)/60/60)>(select time_enable_profile from setting_system where id=1)\n" +
            "  and enabled=0 order by rand() limit 1",nativeQuery = true)
    public ProfileTask get_Profile_Rand_Enable0_And_NotIn(String profile_id,String device_id);

    @Query(value = "Select * from profile_task where  device_id=?1 order by update_time asc limit 1",nativeQuery = true)
    public ProfileTask get_Profile_Get_Task(String device_id);

    @Query(value = "Select count(*) from profile_task where  device_id=?1 and enabled=1 and google_time=0",nativeQuery = true)
    public Integer check_Profile_Enabled_And_GoogleLogin(String device_id);

    @Query(value = "Select count(*) from profile_task where  device_id=?1 and enabled=1",nativeQuery = true)
    public Integer check_Profile_Enabled(String device_id);

    @Query(value = "Select * from profile_task where  device_id=?1 and enabled=1 and profile_id!=?2  order by update_time asc limit 1",nativeQuery = true)
    public ProfileTask get_Profile_Get_Task_By_Enabled(String device_id,String profile_id);

    @Query(value = "Select * from profile_task where profile_id=?1 limit 1",nativeQuery = true)
    public ProfileTask get_Profile_By_ProfileId(String profile_id);

    @Query(value = "Select count(*) from profile_task where  device_id=?1 and enabled=1 order by update_time asc limit 1",nativeQuery = true)
    public Integer get_Count_Profile_Enabled(String device_id);

    @Query(value = "Select * from profile_task where profile_id=?1 and task_index>0 and platform!='' and account_id!='' limit 1",nativeQuery = true)
    public ProfileTask check_ProfileId_Running(String profile_id);

    @Query(value = "Select update_time from profile_task where  device_id=?1  order by update_time desc limit 1;",nativeQuery = true)
    public Long get_Update_Time_Desc(String device_id);

    @Query(value = "Select * from profile_task where profile_id=?1 and state=1 limit 1",nativeQuery = true)
    public ProfileTask check_ProfileId_State(String profile_id);

    @Query(value = "SELECT p FROM ProfileTask p JOIN FETCH p.device where p.device.device_id=?1 order by p.update_time desc")
    public List<ProfileTask> get_Profile_By_DeviceId(String device_id);

    @Query(value = "SELECT p FROM ProfileTask p JOIN FETCH p.device where p.profile_id=?1")
    public ProfileTask get_Profile_By_ProfileId_JOIN_Device(String profile_id);

    @Query(value = "SELECT COALESCE(MAX(tiktok_lite_version), 0) AS max_value FROM profile_task WHERE  device_id=?1",nativeQuery = true)
    public Long get_Max_Version_Tiktok_By_DeviceId(String device_id);
    @Query(value = "SELECT MAX(tiktok_lite_version) AS max_value FROM profile_task",nativeQuery = true)
    public Long get_Max_Version_Tiktok_In_System();

    @Query(value = "SELECT count(*) FROM profile_task where valid=0 and device_id=?1",nativeQuery = true)
    public Integer get_Count_Profile_Valid_0_By_DeviceId(String device_id);
    @Query(value = "SELECT profile_id FROM profile_task where valid=0 and device_id=?1 limit 1",nativeQuery = true)
    public String get_ProfileId_Valid_0_By_DeviceId(String device_id);

    @Query(value = "SELECT REPLACE(profile_id, concat(device_id,'_'), '') FROM profile_task where reboot=1 and device_id=?1 order by update_time desc limit 1",nativeQuery = true)
    public String get_ProfileId_Reboot_1_By_DeviceId(String device_id);

    @Query(value = "SELECT GROUP_CONCAT(platform) AS concatenated_rows FROM Data.account_profile where live=1 and profile_id=?1",nativeQuery = true)
    public String get_AccountLive_By_ProfileId(String profile_id);

    @Query(value = "SELECT profile_id,GROUP_CONCAT(platform) AS concatenated_rows FROM Data.account_profile where live=1 and profile_id in (select profile_id from profile_task where device_id=?1) group by profile_id",nativeQuery = true)
    public List<Object[]> get_AccountLive_GroupBy_ProfileId_By_DeviceId(String device_id);

    @Query(value = "SELECT GROUP_CONCAT(platform) AS concatenated_rows FROM Data.account where live>1 and profile_id=?1",nativeQuery = true)
    public String get_AccountDie_By_ProfileId(String profile_id);

    @Query(value = "SELECT count(*) FROM Data.profile_task where order_id=?1 and running=1 and round((UNIX_TIMESTAMP()-task_time/1000)/60)<=10",nativeQuery = true)
    public Integer get_Count_Thread_By_OrderId(Long order_id);

    @Query(value = "SELECT profile_id,GROUP_CONCAT(platform) AS concatenated_rows FROM Data.account where live>1 and device_id=?1 group by profile_id",nativeQuery = true)
    public List<Object[]> get_AccountDie_GroupBy_ProfileId_By_DeviceId(String device_id);

    @Query(value = "SELECT GROUP_CONCAT(platform) AS concatenated_rows FROM Data.account_profile where live=1 and profile_id like ?1",nativeQuery = true)
    public String get_AccountLive_By_DeviceId(String device_id);
    @Query(value = "SELECT GROUP_CONCAT(platform) AS concatenated_rows FROM Data.account where live>1 and profile_id like ?1",nativeQuery = true)
    public String get_AccountDie_By_DeviceId(String device_id);
    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0,order_id=0,task='',task_key='',task_index=0,task_list='',request_index=0,account_id='',platform='' where profile_id=?1",nativeQuery = true)
    public Integer reset_Thread_Index_By_ProfileId(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET reboot=0 where device_id=?1",nativeQuery = true)
    public Integer reset_Reboot_By_DeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET reboot=0 where profile_id=?1",nativeQuery = true)
    public Integer reset_Reboot_By_ProfileId(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0,order_id=0,task='',task_key='',task_index=0,task_list='',request_index=0,account_id='',platform='' where device_id=?1",nativeQuery = true)
    public Integer reset_Thread_Index_By_DeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0,order_id=0,task='',task_key='',task_index=0,task_list='',request_index=0,register_index=0,account_id='',platform='',state=0,add_proxy=0 where device_id=?1",nativeQuery = true)
    public Integer reset_Thread_Index_By_DeviceId_While_ChangerProfile(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0,order_id=0,task='',task_key='',task_index=0,task_list='',request_index=0,register_index=0,account_id='',platform='',state=0 where device_id=?1",nativeQuery = true)
    public Integer reset_Thread_Index_By_DeviceId_While_ChangerProfile_1_On(String device_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE  FROM profile_task  where profile_id not in(?1) and device_id=?2",nativeQuery = true)
    public Integer delete_Profile_Not_In(List<String>  list_profile, String device_id);
    @Modifying
    @Transactional
    @Query(value = "DELETE  FROM profile_task  where profile_id in(?1) and device_id=?2",nativeQuery = true)
    public Integer delete_Profile_In(List<String>  list_profile, String device_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE  FROM profile_task  where profile_id in(?1) and device_id=?2 and valid=0",nativeQuery = true)
    public Integer delete_Profile_In_And_Valid0(List<String>  list_profile, String device_id);
    @Modifying
    @Transactional
    @Query(value = "SELECT profile_id FROM profile_task  where profile_id not in(?1) and device_id=?2",nativeQuery = true)
    public List<String> get_ProfileId_Not_In(List<String>  list_profile, String device_id);

    @Modifying
    @Transactional
    @Query(value = "SELECT profile_id FROM profile_task  where profile_id not in(?1) and device_id=?2 and valid=0",nativeQuery = true)
    public List<String> get_ProfileId_Not_In_And_Valid0(List<String>  list_profile, String device_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE  FROM profile_task  where profile_id not in(?1) and device_id=?2 and valid=0",nativeQuery = true)
    public Integer delete_Profile_Not_In_And_Valid0(List<String>  list_profile, String device_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE  FROM profile_task  where  device_id in (?1)",nativeQuery = true)
    public Integer delete_Profile_By_List_Device(List<String> device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task set enabled=1, enabled_time=?2 where profile_id=?1",nativeQuery = true)
    public Integer update_Enabled_Profile_By_ProfileId(String profile_id,Long enabled_time);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task set valid=?1 where profile_id=?2",nativeQuery = true)
    public Integer update_Valid_Profile_By_ProfileId(Integer valid,String profile_id);

    @Query(value = "SELECT * FROM profile_task where get_time<=update_time and device_id=?1 and enabled=1 order by update_time asc limit 1",nativeQuery = true)
    public ProfileTask get_ProfileId_Can_Running_By_DeviceId(String device_id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0,order_id=0,task='',task_key='' where profile_id in (select profile_id from account_profile where account_id=?1)",nativeQuery = true)
    public Integer reset_Thread_By_AccountId(String account_id);

    @Modifying
    @Transactional
    @QueryHints({ @QueryHint(name = "javax.persistence.query.timeout", value = "2000") }) // 2 giây
    @Query("UPDATE ProfileTask p SET p.running = 0 WHERE p.profile_id=?1")
    void reset_Thread_By_ProfileId(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET task_index=(select priority from platform where platform=?1 limit 1) where profile_id in(select profile_id from account_profile where account_id=?2)",nativeQuery = true)
    public Integer update_Than_Task_Index_By_AccountId(String platform,String account_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET task_index=(select priority from platform where platform=?1 limit 1) where profile_id=?1",nativeQuery = true)
    public Integer update_Than_Task_Index_By_ProofileId(String platform,String profile_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account_task SET running=0,order_id=0,task='',task_key='',task_index=0,task_list='' where account_id=?1",nativeQuery = true)
    public Integer reset_Thread_Index_By_AccountId(String account_id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0 where round((UNIX_TIMESTAMP()-get_time/1000)/60)>5 and running=1 limit 50",nativeQuery = true)
    public Integer reset_Task_Error();

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0 where profile_id=?1",nativeQuery = true)
    public Integer reset_Task_By_ProfileId(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0 where device_id=?1",nativeQuery = true)
    public Integer reset_Task_By_deviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0 where device_id=?1",nativeQuery = true)
    public Integer reset_Task_By_DeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "delete account_task where account_id=?1",nativeQuery = true)
    public Integer delete_Account_By_AccountId(String account_id);

}
