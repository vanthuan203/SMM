package com.nts.awspremium.repositories;

import com.nts.awspremium.model.SettingInstagram;
import com.nts.awspremium.model.SettingX;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SettingInstagramRepository extends JpaRepository<SettingInstagram,Long> {
    @Query(value = "SELECT * FROM setting_instagram where id=1",nativeQuery = true)
    public SettingInstagram get_Setting();

}
