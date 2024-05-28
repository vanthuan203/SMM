package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountTask;
import com.nts.awspremium.model.Box;
import com.nts.awspremium.model.Device;
import com.nts.awspremium.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface ProfileRepository extends JpaRepository<Profile,String> {

    @Query(value = "Select barcode from profile where profile_id=?1 limit 1",nativeQuery = true)
    public String check_Barcode_By_ProfileId(String device_id);
    @Query(value = "select count(*) from profile where device_id=?1",nativeQuery = true)
    public Integer count_Profile_By_DeviceId(String device_id);

    @Query(value = "select count(*) from profile where device_id=?1",nativeQuery = true)
    public Integer check_Profile_By_DeviceId(String device_id);

    @Query(value = "Select * from profile where state=-1 order by time_update asc limit 1",nativeQuery = true)
    public Profile find_Profile_Changer_Profile();
    @Query(value = "Select * from profile where profile_id=?1 limit 1",nativeQuery = true)
    public Profile find_Profile_Changer_Profile(String profile_id);

    @Query(value = "Select * from profile where profile_id=?1 limit 1",nativeQuery = true)
    public Profile check_ProfileId(String profile_id);

    @Query(value = "Select * from profile where num_account<(SELECT max_acc FROM Data.setting_system) and device_id=?1 order by update_time asc limit 1",nativeQuery = true)
    public Profile get_Profile_Get_Account_By_DeviceId(String device_id);

    @Query(value = "Select * from profile where num_account>0 and device_id=?1 order by update_time asc limit 1",nativeQuery = true)
    public Profile get_Profile_Get_Task(String device_id);

    @Query(value = "Select count(*) from profile where profile_id=?1 limit 1",nativeQuery = true)
    public Integer count_ProfileId(String profile_id);

}
