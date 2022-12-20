package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderBuffhRunning;
import com.nts.awspremium.model.VideoBuffh;
import com.nts.awspremium.model.VideoBuffhHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoBuffhHistoryRepository extends JpaRepository<VideoBuffhHistory,Long> {

    @Query(value = "SELECT * from videobuffhhistory order by enddate desc",nativeQuery = true)
    public List<VideoBuffhHistory> getVideoBuffhHistories();

    @Query(value = "Select *\n" +
            "from videobuffhhistory where CONCAT(videoid,'-',videotitle,'-',\n" +
            "channelid,'-',maxthreads,'-',timebuff,'-',note,'-',optionbuff) like ?1",nativeQuery = true)
    public List<VideoBuffhHistory> getOrderHistoryFilter(String key);

    @Modifying
    @Transactional
    @Query(value = "update videobuffhhistory set viewend=?1 where videoid=?2",nativeQuery = true)
    public Integer updateviewend(Integer viewend,String videoid);

    @Query(value = "SELECT videoid FROM videobuffhhistory where viewend is null and round((UNIX_TIMESTAMP()-enddate/1000)/60/60)>=5 limit 50",nativeQuery = true)
    public List<String> getOrderHistorythan5h();
}
