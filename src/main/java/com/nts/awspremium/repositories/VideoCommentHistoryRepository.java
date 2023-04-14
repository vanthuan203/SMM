package com.nts.awspremium.repositories;

import com.nts.awspremium.model.VideoCommentHistory;
import com.nts.awspremium.model.VideoViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoCommentHistoryRepository extends JpaRepository<VideoCommentHistory,Long> {
    @Query(value = "SELECT * from videocommenthistory where orderid in (?1)",nativeQuery = true)
    public List<VideoCommentHistory> getVideoViewHisByListId(List<String> list_orderid);

    @Query(value = "SELECT * from videocommenthistory where orderid=?1",nativeQuery = true)
    public VideoCommentHistory getVideoViewHisById(Long orderid);
    @Query(value = "SELECT videoid FROM videoviewhistory where viewend is null and service in(111,112,113,122,123,801,802,811,812,813) and cancel!=1 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60)>=5 order by enddate desc limit 50",nativeQuery = true)
    public List<String> getOrderHistorythan5h();

    @Query(value = "SELECT * FROM videoviewhistory where videoid=?1 and service in(111,112,113,122,123,801,802,811,812,813) and \n" +
            "orderid in( SELECT  * FROM (SELECT  MAX(orderid) FROM videoviewhistory where user!='baohanh01@gmail.com' group by videoid) as p) limit 1",nativeQuery = true)
    public List<VideoCommentHistory> getVideoBHByVideoId(String videoid);

    @Query(value = "SELECT * FROM videoviewhistory where orderid=?1 and service in(111,112,113,122,123,801,802,811,812,813) and user!='baohanh01@gmail.com' limit 1",nativeQuery = true)
    public List<VideoCommentHistory> getVideoBHByOrderId(Long orderid);

    @Modifying
    @Transactional
    @Query(value = "update videoviewhistory set viewend=?1 where videoid=?2",nativeQuery = true)
    public Integer updateviewend(Integer viewend,String videoid);

    @Query(value = "SELECT * from videoviewhistory where round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=20 order by enddate desc",nativeQuery = true)
    public List<VideoCommentHistory> getVideoViewHistories();

    @Query(value = "SELECT * from videoviewhistory where round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=20 and videoid=?1 order by enddate desc",nativeQuery = true)
    public List<VideoCommentHistory> getVideoViewHistoriesByVideoId(String videoid);

    @Query(value = "SELECT * from videoviewhistory where user=?1 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=20 order by enddate desc",nativeQuery = true)
    public List<VideoCommentHistory> getVideoViewHistories(String user);



}
