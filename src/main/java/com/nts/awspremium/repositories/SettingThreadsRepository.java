package com.nts.awspremium.repositories;

import com.nts.awspremium.model.SettingThreads;
import com.nts.awspremium.model.SettingX;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SettingThreadsRepository extends JpaRepository<SettingThreads,Long> {
    @Query(value = "SELECT * FROM setting_threads where id=1",nativeQuery = true)
    public SettingThreads get_Setting();

}
