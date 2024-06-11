package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountTask;
import com.nts.awspremium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountTaskRepository extends JpaRepository<AccountTask,String> {
    @Query(value = "SELECT account_task.*  from account_task\n" +
            "join profile on profile.profile_id=account_task.profile_id\n" +
            "join device on device.device_id=?1 \n" +
            "where account_task.state=0 and account_task.running=1 limit 1",nativeQuery = true)
    public AccountTask find_AccountTask_Changer_Profile(String device_id);

    @Query(value = "Select * from account_task where state=-1 and running=1 order by time_get asc limit 1",nativeQuery = true)
    public AccountTask find_AccountTask_Changer_Profile();

    @Query(value = "Select count(*) from account_task where account_id=?1",nativeQuery = true)
    public Integer find_AccountTask_By_AccountId(String account_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account_task SET running=0,order_id=0,task='',task_key='' where account_id=?1",nativeQuery = true)
    public Integer reset_Thread_By_AccountId(String account_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account_task SET running=0,order_id=0,task='',task_key='',task_index=0 where account_id=?1",nativeQuery = true)
    public Integer reset_Thread_Index_By_AccountId(String account_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account_task SET running=0,order_id=0,task='',task_key='',task_index=0 where profile_id=?1",nativeQuery = true)
    public Integer reset_Thread_Index_By_ProfileId(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account_task SET running=0,order_id=0,task='',task_key='',task_index=0 where device_id=?1",nativeQuery = true)
    public Integer reset_Thread_Index_By_DeviceId(String device_id);

    @Query(value = "SELECT * FROM account_task where account_id=?1 limit 1",nativeQuery = true)
    public AccountTask get_Account_By_Account_id(String account_id);
    @Query(value = "SELECT * FROM account_task where task_index>0 and device_id=?1 limit 1",nativeQuery = true)
    public AccountTask check_Account_Running_By_DeviceId(String device_id);

    @Query(value = "SELECT * FROM account_task where task_index>0 and profile_id=?1 limit 1",nativeQuery = true)
    public AccountTask check_Account_Running_By_ProfileId(String profile_id);
    @Query(value = "SELECT * FROM account_task where device_id=?1 order by get_time asc limit 1",nativeQuery = true)
    public AccountTask get_Account_By_DeviceId(String device_id);

    @Query(value = "SELECT account_task.* FROM account_task join profile on account_task.profile_id=?1 and account_task.get_time<=profile.update_time  order by profile.update_time asc limit 1",nativeQuery = true)
    public AccountTask get_Account_By_ProfileId(String profile_id);
}
