package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderCommentHistory;
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
    @Query(value = "SELECT * from videocommenthistory where  videoid in (?1) or orderid in (?1) order by enddate desc",nativeQuery = true)
    public List<VideoCommentHistory> getVideoViewHistoriesByListVideoId(List<String> list_orderid);
    @Query(value = "SELECT orderid,cancel,enddate,insertdate,videocommenthistory.note,price,videocommenthistory.service,user,videoid,commenttotal,commentend,commentorder,commentstart,geo,lc_code from videocommenthistory left join service on service.service=videocommenthistory.service where round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=10 order by enddate desc\n",nativeQuery = true)
    public List<OrderCommentHistory> getVideoViewHistories();

    @Query(value = "SELECT orderid,cancel,enddate,insertdate,videocommenthistory.note,price,videocommenthistory.service,user,videoid,commenttotal,commentend,commentorder,commentstart,geo,lc_code from videocommenthistory left join service on service.service=videocommenthistory.service where user=?1 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=10 order by enddate desc\n",nativeQuery = true)
    public List<OrderCommentHistory> getVideoViewHistories(String user);
    @Modifying
    @Transactional
    @Query(value = "update videocommenthistory set price=0,commenttotal=0,cancel=1 where orderid in(?1)",nativeQuery = true)
    public void updateRefund(List<String> list_orderid);


}
