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
    public List<VideoViewHistory> getVideoViewHisByListId(List<String> list_orderid);

    @Query(value = "SELECT * from videoviewhistory where orderid=?1",nativeQuery = true)
    public VideoViewHistory getVideoViewHisById(Long orderid);
    @Query(value = "SELECT videoid FROM videoviewhistory where viewend is null and service in(201,202,203,211,212,213) and cancel!=1 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60)>=5 order by enddate desc limit 50",nativeQuery = true)
    public List<String> getOrderHistorythan5h();


    @Query(value = "SELECT * FROM videoviewhistory where videoid=?1 and service in(201,202,203,211,212,213) and \n" +
            "orderid in( SELECT  * FROM (SELECT  MAX(orderid) FROM videoviewhistory where user!='baohanh01@gmail.com' group by videoid) as p) limit 1",nativeQuery = true)
    public List<VideoViewHistory> getVideoBHByVideoId(String videoid);

    @Query(value = "SELECT * FROM videoviewhistory where videoid in(select videoid from videoviewhistory where orderid=?1) and service in(201,202,203,211,212,213) and\n" +
            "            orderid in( SELECT  * FROM (SELECT  MAX(orderid) FROM videoviewhistory where user!='baohanh01@gmail.com' group by videoid) as p) limit 1",nativeQuery = true)
    public List<VideoViewHistory> getVideoBHByOrderId(Long orderid);

    @Modifying
    @Transactional
    @Query(value = "update videoviewhistory set viewend=?1 where videoid=?2",nativeQuery = true)
    public Integer updateviewend(Integer viewend,String videoid);


    @Query(value = "SELECT * from videoviewhistory where round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=20 order by enddate desc",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHistories();
    @Query(value = "SELECT * from videoviewhistory where round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=20 and videoid=?1 order by enddate desc",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHistoriesByVideoId(String videoid);

    @Query(value = "SELECT * from videoviewhistory where user=?1 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=20 order by enddate desc",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHistories(String user);



}
