package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryRegister;
import com.nts.awspremium.model.YoutubeLike24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HistoryRegisterRepository extends JpaRepository<HistoryRegister,Long> {
    @Query(value = "select count(*) from history_register where platform=?1 and profile_id=?2 and round((UNIX_TIMESTAMP()-update_time/1000)/60/60)<?3",nativeQuery = true)
    public Integer count_Register_By_Platform_And_ProfileId(String platform,String profile_id,Integer count);

    @Query(value = "select count(*) from history_register where platform=?1 and profile_id in (select profile_id from profile_task where device_id in(?2)) and round((UNIX_TIMESTAMP()-update_time/1000)/60)<?3",nativeQuery = true)
    public Integer count_Register_By_Platform_And_Time(String platform,List<String> device_id,Integer minute);
}
