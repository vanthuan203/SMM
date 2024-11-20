package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryRegister;
import com.nts.awspremium.model.YoutubeLike24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface HistoryRegisterRepository extends JpaRepository<HistoryRegister,Long> {
    @Query(value = "select count(*) from history_register where platform=?1 and profile_id=?2 and round((UNIX_TIMESTAMP()-update_time/1000)/60/60)<24",nativeQuery = true)
    public Integer count_Register_24h_By_Platform_And_ProfileId(String platform,String profile_id);

    @Query(value = "select count(*) from history_register where platform=?1 and round((UNIX_TIMESTAMP()-update_time/1000)/60)<?2",nativeQuery = true)
    public Integer count_Register_By_Platform_And_Time(String platform,Integer minute);
}
