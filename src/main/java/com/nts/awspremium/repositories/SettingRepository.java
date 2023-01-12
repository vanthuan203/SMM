package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SettingRepository  extends JpaRepository<Setting,Long> {
    @Query(value = "SELECT pricerate FROM setting where id=1",nativeQuery = true)
    public Integer getPrice();

    @Query(value = "SELECT count(*) FROM setting where id=1 and (select count(*) from videobuffh where enabled!=0)<maxorder",nativeQuery = true)
    public Integer getMaxOrder();
    @Query(value = "SELECT * FROM setting where id=1",nativeQuery = true)
    public List<Setting> getSetting();
}
