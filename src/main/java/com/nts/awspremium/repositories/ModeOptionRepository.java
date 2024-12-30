package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ModeOption;
import com.nts.awspremium.model.SettingFacebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ModeOptionRepository extends JpaRepository<ModeOption,Long> {
    @Query(value = "SELECT * FROM mode_option where mode=?1 and platform=?2 and task=?3 limit 1",nativeQuery = true)
    public ModeOption get_Mode_Option(String mode,String platform,String task);

    @Query(value = "SELECT * FROM mode_option ",nativeQuery = true)
    public List<ModeOption> get_List_Mode_Option();


}
