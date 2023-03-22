package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SettingRepository  extends JpaRepository<Setting,Long> {
    @Query(value = "SELECT pricerate FROM setting where id=1",nativeQuery = true)
    public Integer getPrice();

    @Query(value = "SELECT bonus FROM setting where id=1",nativeQuery = true)
    public Integer getBonus();

    @Query(value = "SELECT count(*) FROM setting where id=1 and (select count(*) from videoview where service>600)<maxordervn",nativeQuery = true)
    public Integer getMaxOrderVN();
    @Query(value = "SELECT count(*) FROM setting where id=1 and (select count(*) from videoview where service<600)<maxorderus",nativeQuery = true)
    public Integer getMaxOrderUS();
    @Query(value = "SELECT * FROM setting where id=1",nativeQuery = true)
    public List<Setting> getSetting();
}
