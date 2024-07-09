package com.nts.awspremium.repositories;

import com.nts.awspremium.model.SettingFacebook;
import com.nts.awspremium.model.SettingTiktok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SettingFacebookRepository extends JpaRepository<SettingFacebook,Long> {
    @Query(value = "SELECT * FROM setting_facebook where id=1",nativeQuery = true)
    public SettingFacebook get_Setting();

}
