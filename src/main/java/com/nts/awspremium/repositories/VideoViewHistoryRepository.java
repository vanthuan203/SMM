package com.nts.awspremium.repositories;

import com.nts.awspremium.model.VideoBuffhHistory;
import com.nts.awspremium.model.VideoView;
import com.nts.awspremium.model.VideoViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoViewHistoryRepository extends JpaRepository<VideoViewHistory,Long> {
    @Query(value = "SELECT * from videoviewhistory where orderid in (?1)",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHisByListId(String list_orderid);

    @Query(value = "SELECT videoid FROM videoviewhistory where viewend is null and round((UNIX_TIMESTAMP()-enddate/1000)/60/60)>=5 limit 50",nativeQuery = true)
    public List<String> getOrderHistorythan5h();

    @Modifying
    @Transactional
    @Query(value = "update videoviewhistory set viewend=?1 where videoid=?2",nativeQuery = true)
    public Integer updateviewend(Integer viewend,String videoid);

    @Query(value = "SELECT * from videoviewhistory order by enddate desc",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHistories();

    @Query(value = "SELECT * from videoviewhistory where user=?1 order by enddate desc",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHistories(String user);



}
