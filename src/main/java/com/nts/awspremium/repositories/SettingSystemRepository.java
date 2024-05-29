package com.nts.awspremium.repositories;

import com.nts.awspremium.model.SettingSystem;
import com.nts.awspremium.model.SettingTiktok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface SettingSystemRepository extends JpaRepository<SettingSystem,Long> {
    @Query(value = "SELECT pricerate FROM setting_tiktok where id=1",nativeQuery = true)
    public Integer getPrice();

    @Modifying
    @Transactional
    @Query(value = "update setting set redirectvn=?1 where id=1",nativeQuery = true)
    public void updateRedirectVN(Integer redirect);
    @Query(value = "select count(*) from INFORMATION_SCHEMA.PROCESSLIST where db = 'Data' and COMMAND='Query' and TIME>0",nativeQuery = true)
    public Integer check_MySQL();
    @Modifying
    @Transactional
    @Query(value = "update setting set redirectus=?1 where id=1",nativeQuery = true)
    public void updateRedirectUS(Integer redirect);


    @Modifying
    @Transactional
    @Query(value = "update setting set maxorderbuffhvn=?1 where id=1",nativeQuery = true)
    public void updateMaxRunningBuffHVN(Integer redirect);

    @Modifying
    @Transactional
    @Query(value = "update setting set maxorderbuffhus=?1 where id=1",nativeQuery = true)
    public void updateMaxRunningBuffHUS(Integer redirect);

    @Query(value = "SELECT bonus FROM setting where id=1",nativeQuery = true)
    public Integer getBonus();

    @Query(value = "SELECT count(*) FROM setting where id=1 and (select count(*) from videoview where service>600)<maxordervn",nativeQuery = true)
    public Integer getMaxOrderVN();

    @Query(value = "SELECT count(*) FROM setting where id=1 and (select count(*) from videocomment where service=888)<maxordervn",nativeQuery = true)
    public Integer getMaxOrderCmtVN();

    @Query(value = "SELECT count(*) FROM setting where id=1 and (select count(*) from videocomment where service=222)<maxorderus",nativeQuery = true)
    public Integer getMaxOrderCmtUS();
    @Query(value = "SELECT count(*) FROM setting where id=1 and (select count(*) from videoview where service<600)<maxorderus",nativeQuery = true)
    public Integer getMaxOrderUS();
}
