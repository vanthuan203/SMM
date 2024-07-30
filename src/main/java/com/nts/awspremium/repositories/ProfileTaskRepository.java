package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ProfileShow;
import com.nts.awspremium.model.ProfileTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ProfileTaskRepository extends JpaRepository<ProfileTask,String> {

    @Query(value = "Select * from profile_task where profile_id=?1 limit 1",nativeQuery = true)
    public ProfileTask check_ProfileId(String profile_id);

    @Query(value = "Select * from profile_task where  device_id=?1 order by update_time asc limit 1",nativeQuery = true)
    public ProfileTask get_Profile_Get_Task(String device_id);

    @Query(value = "Select count(*) from profile_task where  device_id=?1 and enabled=1",nativeQuery = true)
    public Integer check_Profile_Enabled(String device_id);

    @Query(value = "Select * from profile_task where  device_id=?1 and enabled=1 order by update_time asc limit 1",nativeQuery = true)
    public ProfileTask get_Profile_Get_Task_By_Enabled(String device_id);

    @Query(value = "Select count(*) from profile_task where  device_id=?1 and enabled=1 order by update_time asc limit 1",nativeQuery = true)
    public Integer get_Count_Profile_Enabled(String device_id);

    @Query(value = "Select * from profile_task where profile_id=?1 and task_index>0 and platform!='' and account_id!='' limit 1",nativeQuery = true)
    public ProfileTask check_ProfileId_Running(String profile_id);

    @Query(value = "Select update_time from profile_task where  device_id=?1  order by update_time desc limit 1;",nativeQuery = true)
    public Long get_Update_Time_Desc(String device_id);

    @Query(value = "Select * from profile_task where profile_id=?1 and state=1 limit 1",nativeQuery = true)
    public ProfileTask check_ProfileId_State(String profile_id);

    @Query(value = "SELECT * FROM profile_task where device_id=?1",nativeQuery = true)
    public List<ProfileShow> get_Profile_By_DeviceId(String device_id);
    @Query(value = "SELECT GROUP_CONCAT(platform) AS concatenated_rows FROM Data.account_profile where live=1 and profile_id=?1",nativeQuery = true)
    public String get_AccountLive_By_ProfileId(String profile_id);
    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0,order_id=0,task='',task_key='',task_index=0,task_list='',request_index=0,account_id='',platform='' where profile_id=?1",nativeQuery = true)
    public Integer reset_Thread_Index_By_ProfileId(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0,order_id=0,task='',task_key='',task_index=0,task_list='',request_index=0,account_id='',platform='',state=0 where device_id=?1",nativeQuery = true)
    public Integer reset_Thread_Index_By_DeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE  FROM profile_task  where profile_id not in(?1) and device_id=?2",nativeQuery = true)
    public Integer delete_Profile_Not_In(List<String>  list_profile, String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task set enabled=1, enabled_time=?2 where profile_id=?1",nativeQuery = true)
    public Integer update_Enabled_Profile_By_ProfileId(String profile_id,Long enabled_time);

    @Query(value = "SELECT * FROM profile_task where get_time<=update_time and device_id=?1 and enabled=1 order by update_time asc limit 1",nativeQuery = true)
    public ProfileTask get_ProfileId_Can_Running_By_DeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0,order_id=0,task='',task_key='' where profile_id in(select profile_id from account_profile where account_id like ?1)",nativeQuery = true)
    public Integer reset_Thread_By_AccountId(String account_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET task_index=(select priority from platform where platform=?1 limit 1) where profile_id in(select profile_id from account_profile where account_id like ?2)",nativeQuery = true)
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
    @Query(value = "UPDATE profile_task SET running=0 where round((UNIX_TIMESTAMP()-get_time/1000)/60)>30",nativeQuery = true)
    public Integer reset_Task_Error();

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0 where profile_id=?1",nativeQuery = true)
    public Integer reset_Task_By_ProfileId(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profile_task SET running=0 where device_id=?1",nativeQuery = true)
    public Integer reset_Task_By_DeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "delete account_task where account_id=?1",nativeQuery = true)
    public Integer delete_Account_By_AccountId(String account_id);

}
