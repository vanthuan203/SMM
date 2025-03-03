package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ModeOption;
import com.nts.awspremium.model.SettingFacebook;
import com.nts.awspremium.model.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ModeOptionRepository extends JpaRepository<ModeOption,Long> {
    @Query(value = "SELECT * FROM mode_option where mode=?1 and platform=?2 and task=?3 limit 1",nativeQuery = true)
    public ModeOption get_Mode_Option(String mode,String platform,String task);

    @Query(value = "SELECT * FROM mode_option where mode=(select mode from device where device_id=(select device_id from account where account_id=?1 and platform=?2 ))  limit 1",nativeQuery = true)
    public ModeOption get_Mode_Option_By_AccountId_And_Platform(String account_id,String platform);

    @Query(value = "SELECT * FROM mode_option ",nativeQuery = true)
    public List<ModeOption> get_List_Mode_Option();

    @Query(value = "SELECT * FROM mode_option where priority>0 and platform=?1 and mode=?2 and state=1",nativeQuery = true)
    public List<ModeOption> get_Priority_Task_By_Platform_And_Mode(String platform,String mode);


}
