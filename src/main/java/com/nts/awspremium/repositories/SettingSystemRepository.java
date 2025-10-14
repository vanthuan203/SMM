package com.nts.awspremium.repositories;

import com.nts.awspremium.model.SettingSystem;
import com.nts.awspremium.model.SettingTiktok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface SettingSystemRepository extends JpaRepository<SettingSystem,Long> {



    @Query(value = "select count(*) from INFORMATION_SCHEMA.PROCESSLIST where db = 'Data' and COMMAND='Query' and TIME>0",nativeQuery = true)
    public Integer check_MySQL();

    @Query(value = "select * from setting_system where id=1",nativeQuery = true)
    public SettingSystem get_Setting_System();

    @Query(value = "select time_waiting_task from setting_system where id=1",nativeQuery = true)
    public Integer get_Time_Waiting_Task();

}
