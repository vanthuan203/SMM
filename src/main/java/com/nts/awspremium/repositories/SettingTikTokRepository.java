package com.nts.awspremium.repositories;

import com.nts.awspremium.model.SettingTiktok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface SettingTikTokRepository extends JpaRepository<SettingTiktok,Long> {
    @Query(value = "SELECT * FROM setting_tiktok where id=1",nativeQuery = true)
    public SettingTiktok get_Setting();

}
