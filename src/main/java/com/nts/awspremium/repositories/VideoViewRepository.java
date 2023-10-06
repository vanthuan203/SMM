package com.nts.awspremium.repositories;


import com.nts.awspremium.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoViewRepository extends JpaRepository<VideoView,Long> {

    @Query(value = "SELECT * FROM videoview where service in(select service from service where geo=?1 and checktime=0  and live=0) and INSTR(?2,videoid)=0 and orderid in (?3) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewByGeo(String geo, String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service in(select service from service where geo=?1 and checktime=1 and live=0) and INSTR(?2,videoid)=0 and orderid in (?3) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoBuffHByGeo(String geo, String listvideo, List<String> orderid);
    @Query(value = "SELECT * FROM videoview where service>600 and service not in(751,752,753,754) and service not in (select service from service where mintime>=30 or live=1) and INSTR(?1,videoid)=0 and  orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2VNTEST(String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service>600 and service in (select service from service where checktime=1 and mintime>=30 and live=0) and INSTR(?1,videoid)=0 and  orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoBuffHVer2VNTEST(String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service<600 and service not in(151,152,153,154) and service not in (select service from service where mintime>=30 or live=1) and INSTR(?1,videoid)=0 and  orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2USTEST(String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service in(151,152,153,154) and  INSTR(?1,videoid)=0 and  orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2USTESTNoProxy(String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service in(751,752,753,754) and INSTR(?1,videoid)=0 and  orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2VNTESTNoProxy(String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service<600 and service in (select service from service where checktime=1 and mintime>=30 and live=0) and INSTR(?1,videoid)=0 and  orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoBuffHVer2USTEST(String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service>600 and service in (select service from service where live=1) and INSTR(?1,videoid)=0 and  orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoLiveVer2VNTEST(String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service<600 and service in (select service from service where live=1) and INSTR(?1,videoid)=0 and  orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoLiveVer2USTEST(String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service>600 and service in (select service from service where live=1) and  orderid in (?1) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoPreVer2VNTEST(List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service<600 and service in (select service from service where live=1) and  orderid in (?1) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoPreVer2USTEST(List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service>600 and service in (select service from service where live=1) and round((insertdate/1000-UNIX_TIMESTAMP())/60)<=30 limit 1",nativeQuery = true)
    public List<VideoView> getvideoPreTrue();

    @Query(value = "select orderid from (select videoview.orderid,count(*) as total,maxthreads\n" +
            "             from videoview left join historyview on historyview.orderid=videoview.orderid and running=1\n" +
            "             group by orderid having total<maxthreads) as t",nativeQuery = true)
    public List<String> getListOrderTrueThread();

    @Query(value = "SELECT * from videoview where orderid in (?1)",nativeQuery = true)
    public List<VideoView> getVideoViewByListId(List<String> list_orderid);
    @Query(value = "SELECT * from videoview where orderid=?1",nativeQuery = true)
    public VideoView getVideoViewById(Long orderid);

    @Query(value = "SELECT * from videoview where videoid=?1",nativeQuery = true)
    public VideoView getVideoViewByVideoid(String videoid);


    @Query(value = "SELECT count(*) from videoview where user=?1",nativeQuery = true)
    public Integer getCountOrderByUser(String user);

    @Query(value = "SELECT sum(vieworder) from videoview where user=?1 and service=?2 and maxthreads=-1",nativeQuery = true)
    public Integer getCountOrderByUserAndService(String user,Integer service);

    @Query(value = "SELECT sum(vieworder) from videoview where user=?1 and service=?2 and maxthreads!=-1",nativeQuery = true)
    public Integer getCountOrderRunningByUserAndService(String user,Integer service);

    @Query(value = "SELECT sum(vieworder) from videoview where service=?1 and maxthreads!=-1",nativeQuery = true)
    public Integer getCountOrderRunningByService(Integer service);

    @Query(value = "SELECT count(*) from videoview where service=?1",nativeQuery = true)
    public Integer getCountOrderByService(Integer service);

    @Query(value = "SELECT count(*) from videoview where videoid=?1",nativeQuery = true)
    public Integer getCountVideoId(String videoid);

    @Query(value = "SELECT count(*) from videoview where videoid=?1 and user!='baohanh01@gmail.com'",nativeQuery = true)
    public Integer getCountVideoIdNotIsBH(String videoid);

    @Query(value = "SELECT sum(maxthreads)FROM AccPremium.videoview where service in(select service from service where geo='vn' and checktime=0)",nativeQuery = true)
    public Integer getCountThreadView();

    @Query(value = "SELECT * FROM  videoview where service in(select service from service where checktime=0 ) order by timeupdate asc",nativeQuery = true)
    public List<VideoView> getAllOrderView();

    @Query(value = "SELECT * FROM  videoview where service in(select service from service where checktime=1) order by timeupdate asc",nativeQuery = true)
    public List<VideoView> getAllOrderBuffh();


    @Query(value = "SELECT * FROM videoview where maxthreads=0 order by priority desc,insertdate asc",nativeQuery = true)
    public List<VideoView> getAllOrderPending();

    @Query(value = "SELECT * FROM videoview where maxthreads=-1 and user=?1 and vieworder<=?2 order by priority desc,insertdate asc limit ?3",nativeQuery = true)
    public List<VideoView> getAllOrderPending701(String user,Integer vieworder,Integer limit);

    @Query(value = "SELECT * FROM videoview where maxthreads=-1 and vieworder<=?1 order by priority desc,insertdate asc limit ?2",nativeQuery = true)
    public List<VideoView> getAllOrderPending701(Integer vieworder,Integer limit);

    @Query(value = "SELECT * FROM videoview where maxthreads=-1 order by priority desc,insertdate asc",nativeQuery = true)
    public List<VideoView> getAllOrderPending701();

    @Query(value = "SELECT * FROM videoview where maxthreads=0 and service in (select service from service where live=1) and viewtotal=0 order by insertdate asc",nativeQuery = true)
    public List<VideoView> getAllOrderLivePending();

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where user!='baohanh01@gmail.com' and timestart!=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrder();

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service,priority from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where timestart=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrderPending();

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where videoview.valid=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrderCheckCancel();

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where user=?1 and timestart!=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrder(String user);

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service,priority from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where user=?1 and timestart=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrderPending(String user);

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where user=?1 and videoview.valid=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrderCheckCancel(String user);

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service,priority from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where videoview.videoid=?1",nativeQuery = true)
    public List<OrderViewRunning> getVideoViewById(String videoid);

    @Query(value = "SELECT videoview.videoid,count(*) as view FROM historyviewsum left join videoview on historyviewsum.videoid=videoview.videoid" +
            " where  time>=videoview.insertdate and service in(select service from service where checktime=0) group by videoview.videoid order by insertdate desc",nativeQuery = true)
    public List<String> getTotalViewBuff();

    @Query(value = "SELECT videoview.videoid,count(*) as view FROM historyviewsum left join videoview on historyviewsum.videoid=videoview.videoid where time>=videoview.insertdate and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by videoview.videoid order by insertdate desc",nativeQuery = true)
    public List<String> get24hViewBuff();


    @Query(value = "SELECT count(*) FROM videoview where videoid=?1 and service in(select service from service where geo=?2)",nativeQuery = true)
    public Integer getServiceByVideoId(String videoid,String geo);
    @Modifying
    @Transactional
    @Query(value = "UPDATE videoview set viewstart=?1,maxthreads=?2,timestart=?3 where videoid=?4",nativeQuery = true)
    public void updatePendingOrderByVideoId(Integer viewstart,Integer maxthreads,Long timestart,String videoid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE videoview set viewtotal=?1,view24h=?2,timeupdate=?3,maxthreads=?4 where videoid=?5",nativeQuery = true)
    public void updateViewOrderByVideoId(Integer viewtotal,Integer view24h,Long timeupdate,Integer maxthreads,String videoid);

    @Modifying
    @Transactional
    @Query(value = "update videoview set valid=0 where videoid not in (select videoid from historyviewsum where round((UNIX_TIMESTAMP()-time/1000)/60)<=5  group by videoid ) and round((UNIX_TIMESTAMP()-insertdate/1000)/60)>20",nativeQuery = true)
    public void updateOrderCheckCancel();

    @Modifying
    @Transactional
    @Query(value = "update videoview set valid=1 where videoid=?1",nativeQuery = true)
    public void updateOrderCheck(String videoid);

    @Query(value = "SELECT * FROM videoview where valid=0 order by insertdate asc",nativeQuery = true)
    public List<VideoView> getAllOrderCheckCancel();

    @Modifying
    @Transactional
    @Query(value = "update videoview set valid=0 where videoid=?1 and valid=1",nativeQuery = true)
    public void updateCheckCancel(String videoid);

    @Modifying
    @Transactional
    @Query(value = "update videoview set valid=1 where  valid=0",nativeQuery = true)
    public void updateCheckCancelDone();

    @Query(value = "SELECT sum(vieworder) as total FROM videoview",nativeQuery = true)
    public Integer getCountViewBuffOrder();


    @Query(value = "SELECT *  FROM videoview where orderid=?1 limit 1",nativeQuery = true)
    public VideoView getInfoByOrderId(Long orderid);

    @Query(value = "SELECT sum(vieworder) as total FROM videoview where user=?1",nativeQuery = true)
    public Integer getCountViewBuffOrder(String user);

    @Query(value = "SELECT count(*) as viewbuff FROM historyviewsum left join videoview on historyviewsum.videoid=videoview.videoid where historyviewsum.duration>0 and time>=videoview.insertdate",nativeQuery = true)
    public Integer getCountViewBuffedOrder();

    @Query(value = "SELECT count(*) as viewbuff FROM historyviewsum left join videoview on historyviewsum.videoid=videoview.videoid where historyviewsum.duration>0 and videoview.user=?1  time>=videoview.insertdate",nativeQuery = true)
    public Integer getCountViewBuffedOrder(String user);

    @Query(value = "SELECT * from videoview  where videoid=?1 limit 1",nativeQuery = true)
    public List<VideoView> getVideoBuffhById(String videoid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM videoview where videoid=?1",nativeQuery = true)
    public void deletevideoByVideoId(String videoid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM videoview where videoid=?1 and user='baohanh01@gmail.com'",nativeQuery = true)
    public void deletevideoByVideoIdBH(String videoid);

    @Modifying
    @Transactional
    @Query(value = "update videoview set maxthreads=maxthreads+cast(threadset*0.16 as UNSIGNED) where service in (select service from service where maxtime<=5) and maxthreads<threadset and maxthreads>0 and timestart>0;",nativeQuery = true)
    public void updateThreadByThreadSet5m();
    @Modifying
    @Transactional
    @Query(value = "update videoview set maxthreads=maxthreads+cast(threadset*0.09 as UNSIGNED) where service in (select service from service where maxtime=10) and maxthreads<threadset and maxthreads>0 and timestart>0;",nativeQuery = true)
    public void updateThreadByThreadSet10m();

    @Modifying
    @Transactional
    @Query(value = "update videoview set maxthreads=maxthreads+cast(threadset*0.06 as UNSIGNED) where service in (select service from service where maxtime=15) and maxthreads<threadset and maxthreads>0 and timestart>0;",nativeQuery = true)
    public void updateThreadByThreadSet15m();

    @Modifying
    @Transactional
    @Query(value = "update videoview set maxthreads=maxthreads+cast(threadset*0.05 as UNSIGNED) where service in (select service from service where maxtime=20) and maxthreads<threadset and maxthreads>0 and timestart>0;",nativeQuery = true)
    public void updateThreadByThreadSet20m();

    @Modifying
    @Transactional
    @Query(value = "update videoview set maxthreads=maxthreads+cast(threadset*0.03 as UNSIGNED) where service in (select service from service where maxtime>=30) and maxthreads<threadset and maxthreads>0 and timestart>0;",nativeQuery = true)
    public void updateThreadByThreadSet30m();
    @Query(value = "select * from videoview where viewtotal>(vieworder + vieworder*(select bonus/100 from setting where id=1)) and service in(select service from service where checktime=0 and live=0)",nativeQuery = true)
    public List<VideoView> getOrderFullView();

    @Query(value = "select * from videoview where viewtotal>(vieworder + vieworder*(select bonus/100 from setting where id=1)) and service in(select service from service where live=1) and round((UNIX_TIMESTAMP()-timestart/1000)/60)>=(minstart+5)",nativeQuery = true)
    public List<VideoView> getOrderFullLive();

    @Query(value = "select * from videoview where timetotal>(1800*vieworder + 1800*vieworder*(select 2*bonus/100 from setting where id=1))  and viewtotal>(vieworder + vieworder*(select 2*bonus/100 from setting where id=1)) and service in(select service from service where mintime=120 and checktime=1)",nativeQuery = true)
    public List<VideoView> getOrderFullTime120m();
    @Query(value = "select * from videoview where timetotal>(1800*vieworder + 1800*vieworder*(select 2*bonus/100 from setting where id=1))  and viewtotal>(vieworder + vieworder*(select 2*bonus/100 from setting where id=1)) and service in(select service from service where mintime=60 and checktime=1)",nativeQuery = true)
    public List<VideoView> getOrderFullTime60m();
    @Query(value = "select * from videoview where timetotal>(1800*vieworder + 1800*vieworder*(select 2*bonus/100 from setting where id=1))  and viewtotal>(vieworder + vieworder*(select 2*bonus/100 from setting where id=1)) and service in(select service from service where mintime=30 and checktime=1)",nativeQuery = true)
    public List<VideoView> getOrderFullTime30m();

    @Query(value = "select * from videoview where timetotal>(900*vieworder + 900*vieworder*(select 2*bonus/100 from setting where id=1))  and viewtotal>(vieworder + vieworder*(select 2*bonus/100 from setting where id=1)) and service in(select service from service where mintime=15 and checktime=1)",nativeQuery = true)
    public List<VideoView> getOrderFullTime15m();

    @Query(value = "select * from videoview where timetotal>(600*vieworder + 600*vieworder*(select 2*bonus/100 from setting where id=1))  and viewtotal>(vieworder + vieworder*(select 2*bonus/100 from setting where id=1)) and service in(select service from service where maxtime=10 and checktime=1)",nativeQuery = true)
    public List<VideoView> getOrderFullTime10m();


    @Query(value = "SELECT videoview.videoid,sum(historyviewsum.duration) as total,count(*) as view FROM historyviewsum left join videoview on historyviewsum.videoid=videoview.videoid where  time>=videoview.insertdate and service in(select service from service where checktime=1) group by videoview.videoid order by insertdate desc",nativeQuery = true)
    public List<String> getTimeBuffVideo();

    @Query(value = "SELECT sum(maxthreads) FROM AccPremium.videoview where service in(select service from service where live=1)",nativeQuery = true)
    public Integer getSumThreadLive();

    @Modifying
    @Transactional
    @Query(value = "UPDATE videoview set timetotal=?1,viewtotal=?2 where videoid=?3",nativeQuery = true)
    public void updateTimeViewOrderByVideoId(Integer timetotal,Integer viewtotal, String videoid);

}
