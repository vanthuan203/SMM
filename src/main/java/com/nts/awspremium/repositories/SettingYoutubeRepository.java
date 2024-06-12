package com.nts.awspremium.repositories;

import com.nts.awspremium.model.SettingTiktok;
import com.nts.awspremium.model.SettingYoutube;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface SettingYoutubeRepository extends JpaRepository<SettingYoutube,Long> {
    @Query(value = "SELECT * FROM setting_youtube where id=1",nativeQuery = true)
    public SettingYoutube get_Setting();
}
