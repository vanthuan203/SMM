package com.nts.awspremium.repositories;

import com.nts.awspremium.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile,String> {

    @Query(value = "Select barcode from profile where profile_id=?1 limit 1",nativeQuery = true)
    public String check_Barcode_By_ProfileId(String device_id);
    @Query(value = "select count(*) from profile where device_id=?1",nativeQuery = true)
    public Integer count_Profile_By_DeviceId(String device_id);

    @Query(value = "select count(*) from profile where device_id=?1",nativeQuery = true)
    public Integer check_Profile_By_DeviceId(String device_id);

    @Query(value = "SELECT p.*,a.running as running,a.get_time,a.platform,a.task FROM profile p  left join account_task a on p.profile_id=a.profile_id and a.running=1 where p.device_id=?1",nativeQuery = true)
    public List<ProfileShow> get_Profile_By_DeviceId(String device_id);

    @Query(value = "Select * from profile where state=-1 order by time_update asc limit 1",nativeQuery = true)
    public Profile find_Profile_Changer_Profile();
    @Query(value = "Select * from profile where profile_id=?1 limit 1",nativeQuery = true)
    public Profile find_Profile_Changer_Profile(String profile_id);

    @Query(value = "Select * from profile where profile_id=?1 limit 1",nativeQuery = true)
    public Profile check_ProfileId(String profile_id);

    @Query(value = "Select * from profile where num_account<(SELECT max_acc FROM Data.setting_system) and device_id=?1 order by update_time asc limit 1",nativeQuery = true)
    public Profile get_Profile_Get_Account_By_DeviceId(String device_id);

    @Query(value = "Select * from profile where  device_id=?1 order by update_time asc limit 1",nativeQuery = true)
    public Profile get_Profile_Get_Task(String device_id);

    @Query(value = "Select count(*) from profile where profile_id=?1 limit 1",nativeQuery = true)
    public Integer count_ProfileId(String profile_id);
    @Modifying
    @Transactional
    @Query(value = "update profile set num_account=IF(num_account>0, num_account-1, 0) where profile_id=?1",nativeQuery = true)
    public Integer reset_Num_Account_By_ProfileId(String profile_id);


}
