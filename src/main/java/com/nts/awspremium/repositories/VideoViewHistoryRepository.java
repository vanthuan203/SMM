package com.nts.awspremium.repositories;

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

    @Query(value = "SELECT videoid FROM videoviewhistory where viewend is null and service in (select service from service where refill=1) and cancel!=1 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60)>=5 order by enddate desc limit 50",nativeQuery = true)
    public List<String> getOrderHistorythan5h();

    @Query(value = "SELECT * FROM videoviewhistory where videoid=?1 and service in (select service from service where refill=1) and \n" +
            "orderid in( SELECT  * FROM (SELECT  MAX(orderid) FROM videoviewhistory where user!='baohanh01@gmail.com' group by videoid) as p) limit 1",nativeQuery = true)
    public List<VideoViewHistory> getVideoBHByVideoId(String videoid);

    @Query(value = "SELECT * FROM videoviewhistory where videoid=?1 and service in (select service from service where refill=1) and user!='baohanh01@gmail.com' and (cancel=0 or (refund>0&&cancel>0)) order by insertdate asc",nativeQuery = true)
    public List<VideoViewHistory> getVideoBHByVideoIdNoMaxOrderId(String videoid);

    @Query(value = "SELECT * FROM videoviewhistory where videoid=(select videoid from videoviewhistory where orderid=?1 limit 1) and service in (select service from service where refill=1) and user!='baohanh01@gmail.com' and (cancel=0 or (refund>0&&cancel>0)) order by insertdate asc",nativeQuery = true)
    public List<VideoViewHistory> getVideoBHByOrderidNoMaxOrderId(Long orderid);

    @Query(value = "SELECT * FROM videoviewhistory where videoid=?1 and service in (select service from service where refill=1) and user!='baohanh01@gmail.com' and cancel=0 order by insertdate asc",nativeQuery = true)
    public List<VideoViewHistory> getVideoBHByVideoIdNoMaxOrderIdCancel0(String videoid);

    @Query(value = "SELECT * FROM videoviewhistory where videoid in(select videoid from videoviewhistory where orderid=?1) and service in (select service from service where refill=1) and\n" +
            "            orderid in( SELECT  * FROM (SELECT  MAX(orderid) FROM videoviewhistory where user!='baohanh01@gmail.com' group by videoid) as p) limit 1",nativeQuery = true)
    public List<VideoViewHistory> getVideoBHByMaxOrderId(Long orderid);

    @Query(value = "SELECT * FROM videoviewhistory where orderid=?1 and service in (select service from service where refill=1) and user!='baohanh01@gmail.com' limit 1",nativeQuery = true)
    public List<VideoViewHistory> getVideoBHByOrderId(Long orderid);

    @Modifying
    @Transactional
    @Query(value = "update videoviewhistory set viewend=?1,timecheckbh=?2 where videoid=?3 and DATE_FORMAT(FROM_UNIXTIME((timestart-3*60*60*1000+24*3*60*60*1000) / 1000), '%Y-%m-%d')=DATE_FORMAT(FROM_UNIXTIME((UNIX_TIMESTAMP())), '%Y-%m-%d')",nativeQuery = true)
    public Integer updateviewend(Integer viewend,Long timecheckbh, String videoid);

    @Modifying
    @Transactional
    @Query(value = "update videoviewhistory set viewend=?1 where videoid=?2 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60)>8",nativeQuery = true)
    public Integer updateviewendthan5h(Integer viewend,String videoid);


    @Query(value = "SELECT * from videoviewhistory where round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=10 order by enddate desc",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHistories();
    @Query(value = "SELECT * from videoviewhistory where  videoid=?1 order by enddate desc",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHistoriesByVideoId(String videoid);

    @Query(value = "SELECT * from videoviewhistory where  videoid in (?1) or orderid in (?1) order by enddate desc",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHistoriesByListVideoId(List<String> list_orderid);
    @Query(value = "SELECT orderid,videoid,price,vieworder,viewstart,viewtotal,service,insertdate,timestart,cancel,enddate,timecheckbh,viewend,user,note from videoviewhistory where videoid=?1 and orderid<?2 order by orderid desc limit 1",nativeQuery = true)
    public String getInfoSumOrderByVideoId(String videoid,Long orderid);

    @Query(value = "SELECT * from videoviewhistory where  orderid=?1 order by enddate desc",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHistoriesByVideoId(Long orderid);

    @Query(value = "SELECT * from videoviewhistory where user=?1 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=10 order by enddate desc",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHistories(String user);

    @Query(value = "select videoid from videoviewhistory where timestart!=0 and timecheckbh=0 and cancel=0 and DATE_FORMAT(FROM_UNIXTIME((timestart-3*60*60*1000+24*3*60*60*1000) / 1000), '%Y-%m-%d')=DATE_FORMAT(FROM_UNIXTIME((UNIX_TIMESTAMP())), '%Y-%m-%d') and service in(select service from service where checktime=1) order by timestart asc limit ?1",nativeQuery = true)
    public List<String> getVideoViewHistoriesCheckViewEnd(Integer limit);

    @Query(value = "select videoid from videoviewhistory where timestart!=0 and timecheckbh=0 and cancel=0 and viewend=0 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60)>8 and service in(select service from service where checktime=1) order by enddate asc limit ?1",nativeQuery = true)
    public List<String> getVideoViewHistoriesCheckViewEndThan5h(Integer limit);


    @Query(value = "SELECT * FROM videoviewhistory where round((UNIX_TIMESTAMP()-enddate/1000)/60/60)>?1 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60)<=?2 and cancel=0 and timecheck!=-1 and user!='baohanh01@gmail.com' and service in (select service from service where refill=1) order by timecheck asc limit ?3",nativeQuery = true)
    public List<VideoViewHistory> getVideoCheckBH(Integer start,Integer end,Integer limit);

    @Query(value = "SELECT * FROM videoviewhistory where videoid=?1 and service in (select service from service where refill=1) and user='baohanh01@gmail.com' and cancel=0 order by enddate desc  limit 1",nativeQuery = true)
    public List<VideoViewHistory> getTimeBHByVideoId(String videoid);

    @Query(value = "SELECT viewstart FROM videoviewhistory where videoid=?1 order by viewstart asc limit 1",nativeQuery = true)
    public Integer getViewStart3701(String videoid);

    @Query(value = "SELECT sum(vieworder)\n" +
            "FROM videoviewhistory\n" +
            "WHERE service=?1 and DATE_FORMAT(FROM_UNIXTIME((timestart-3*60*60*1000) / 1000), '%Y-%m-%d')=DATE_FORMAT(FROM_UNIXTIME((UNIX_TIMESTAMP()-3*60*60)), '%Y-%m-%d') and user=?2 and cancel!=1",nativeQuery = true)
    public Integer getCountOrderDoneByServiceAndUserInOneDay(Integer service,String user);

    @Modifying
    @Transactional
    @Query(value = "update videoviewhistory set price=0,viewtotal=0,cancel=1 where orderid in(?1)",nativeQuery = true)
    public void updateRefund(List<String> list_orderid);

    @Modifying
    @Transactional
    @Query(value = "update videoviewhistory set timecheck=-1 where timecheck!=-1 and user!='baohanh01@gmail.com' and orderid not in( SELECT  * FROM (SELECT  MAX(orderid) FROM  videoviewhistory where user!='baohanh01@gmail.com' group by videoid) as p) and cancel not in(1,2)",nativeQuery = true)
    public Integer updatetimchecknomaxid();
}
