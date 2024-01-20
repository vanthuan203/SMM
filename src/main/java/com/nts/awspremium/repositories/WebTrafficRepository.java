package com.nts.awspremium.repositories;


import com.nts.awspremium.model.OrderTrafficRunning;
import com.nts.awspremium.model.OrderViewRunning;
import com.nts.awspremium.model.WebTraffic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface WebTrafficRepository extends JpaRepository<WebTraffic,Long> {

    @Query(value = "SELECT * FROM webtraffic where service in(select service from service where geo=?1) and INSTR(?2,orderid)=0 and orderid in (?3) order by rand() limit 1",nativeQuery = true)
    public List<WebTraffic> getWebTrafficByGeo(String geo, String listorderid, List<String> orderid);

    @Query(value = "select orderid from (select webtraffic.orderid,count(running) as total,maxthreads,valid,traffictotal,trafficorder,speedup,traffic24h,maxtraffic24h\n" +
            "                                from webtraffic left join historytraffic on historytraffic.orderid=webtraffic.orderid and running=1 where 1140/maxtraffic24h<round((UNIX_TIMESTAMP()-lastcompleted/1000)/60)\n" +
            "                                 group by orderid having (total<maxthreads and traffic24h<maxtraffic24h) ) as t",nativeQuery = true)
    public List<String> getListOrderTrueThreadON();

    @Modifying
    @Transactional
    @Query(value = "UPDATE webtraffic set lastcompleted=?1 where orderid=?2",nativeQuery = true)
    public void updateLastCompletedByOrderId(Long lastcompleted,Long orderid);

    @Query(value = "SELECT count(*) from webtraffic where orderid=?1 and token=?2 ",nativeQuery = true)
    public Integer checkTrueByOrderIdAndToken(Long orderid,String token);

    @Query(value = "SELECT * from webtraffic where orderid in (?1)",nativeQuery = true)
    public List<WebTraffic> getWebTrafficByListId(List<String> list_orderid);
    @Query(value = "SELECT * from webtraffic where orderid=?1",nativeQuery = true)
    public WebTraffic getWebTrafficById(Long orderid);

    @Query(value = "SELECT * from webtraffic where videoid=?1",nativeQuery = true)
    public WebTraffic getWebTrafficByVideoid(String videoid);


    @Query(value = "SELECT count(*) from webtraffic where user=?1",nativeQuery = true)
    public Integer getCountOrderByUser(String user);

    @Query(value = "SELECT sum(trafficorder) from webtraffic where user=?1 and service=?2 and maxthreads=-1",nativeQuery = true)
    public Integer getCountOrderByUserAndService(String user,Integer service);

    @Query(value = "SELECT sum(vieworder) from webtraffic where user=?1 and service=?2 and maxthreads!=-1",nativeQuery = true)
    public Integer getCountOrderRunningByUserAndService(String user,Integer service);

    @Query(value = "SELECT sum(vieworder) from webtraffic where service=?1 and maxthreads!=-1",nativeQuery = true)
    public Integer getCountOrderRunningByService(Integer service);

    @Query(value = "SELECT sum(vieworder) from webtraffic where service in (select service from service where checktime=1) and maxthreads!=-1",nativeQuery = true)
    public Integer getCountOrderRunningByCheckTime();

    @Query(value = "SELECT sum(vieworder) from webtraffic where service in (select service from service where checktime=1 and geo='vn') and maxthreads!=-1",nativeQuery = true)
    public Integer getCountOrderRunningByCheckTimeVN();

    @Query(value = "SELECT sum(vieworder) from webtraffic where service in (select service from service where checktime=1 and geo='us') and maxthreads!=-1",nativeQuery = true)
    public Integer getCountOrderRunningByCheckTimeUS();

    @Query(value = "SELECT count(*) from webtraffic where service=?1",nativeQuery = true)
    public Integer getCountOrderByService(Integer service);

    @Query(value = "SELECT count(*) from webtraffic where videoid=?1",nativeQuery = true)
    public Integer getCountVideoId(String videoid);

    @Query(value = "SELECT count(*) from webtraffic where videoid=?1 and user!='baohanh01@gmail.com'",nativeQuery = true)
    public Integer getCountVideoIdNotIsBH(String videoid);

    @Query(value = "SELECT sum(maxthreads)FROM AccPremium.webtraffic where service in(select service from service where geo='vn' and checktime=0)",nativeQuery = true)
    public Integer getCountThreadView();

    @Query(value = "SELECT sum(maxthreads)FROM AccPremium.webtraffic where service in(select service from service where geo='vn' and checktime=0)",nativeQuery = true)
    public Integer getCountThreadViewVN();

    @Query(value = "SELECT sum(maxthreads)FROM AccPremium.webtraffic where service in(select service from service where geo='us' and checktime=0)",nativeQuery = true)
    public Integer getCountThreadViewUS();

    @Query(value = "SELECT * FROM  webtraffic where service in(select service from service where category='Website') and timestart>0 order by timeupdate asc",nativeQuery = true)
    public List<WebTraffic> getAllOrderTraffic();

    @Query(value = "SELECT * FROM  webtraffic where service in(select service from service where checktime=1) and timestart>0 order by timeupdate asc",nativeQuery = true)
    public List<WebTraffic> getAllOrderBuffh();


    @Query(value = "SELECT * FROM webtraffic where maxthreads=0 and timestart=0 order by priority desc,insertdate asc limit 25",nativeQuery = true)
    public List<WebTraffic> getAllOrderPending();

    @Query(value = "SELECT * FROM webtraffic where maxthreads=-1 and user=?1 and vieworder<=?2 order by priority desc,insertdate asc limit ?3",nativeQuery = true)
    public List<WebTraffic> getAllOrderPending701(String user,Integer vieworder,Integer limit);

    @Query(value = "SELECT * FROM webtraffic where maxthreads=-1 and vieworder<=?1 order by priority desc,insertdate asc limit ?2",nativeQuery = true)
    public List<WebTraffic> getAllOrderPending701(Integer vieworder,Integer limit);

    @Query(value = "SELECT * FROM webtraffic where maxthreads=-1 order by priority desc,insertdate asc",nativeQuery = true)
    public List<WebTraffic> getAllOrderPending701();

    @Query(value = "SELECT * FROM webtraffic where maxthreads=-1 and service in(select service from service where geo='vn') order by priority desc,insertdate asc",nativeQuery = true)
    public List<WebTraffic> getAllOrderPendingBuffHVN();

    @Query(value = "SELECT * FROM webtraffic where maxthreads=-1 and service in(select service from service where geo='us') order by priority desc,insertdate asc",nativeQuery = true)
    public List<WebTraffic> getAllOrderPendingBuffHUS();

    @Query(value = "SELECT * FROM webtraffic where maxthreads=0 and service in (select service from service where live=1) and viewtotal=0 order by insertdate asc",nativeQuery = true)
    public List<WebTraffic> getAllOrderLivePending();

    @Query(value = "Select webtraffic.orderid,webtraffic.link,count(running) as total,maxthreads,insertdate,timestart,note,trafficorder,user,traffictotal,timeupdate,traffic24h,price,service from webtraffic left join historytraffic on historytraffic.orderid=webtraffic.orderid and running=1 where user!='baohanh01@gmail.com' and timestart!=0 group by orderid order by insertdate desc",nativeQuery = true)
    public List<OrderTrafficRunning> getOrder();


    @Query(value = "Select WebTraffic.orderid,WebTraffic.videoid,WebTraffic.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service,priority from WebTraffic left join historyview on historyview.videoid=WebTraffic.videoid and running=1 where timestart=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrderPending();

    @Query(value = "Select WebTraffic.orderid,WebTraffic.videoid,WebTraffic.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from WebTraffic left join historyview on historyview.videoid=WebTraffic.videoid and running=1 where WebTraffic.valid=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrderCheckCancel();

    @Query(value = "Select webtraffic.orderid,webtraffic.link,count(running) as total,maxthreads,insertdate,timestart,note,trafficorder,user,traffictotal,timeupdate,traffic24h,price,service from webtraffic left join historytraffic on historytraffic.orderid=webtraffic.orderid and running=1 where user=?1 and timestart!=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderTrafficRunning> getOrder(String user);

    @Query(value = "Select WebTraffic.orderid,WebTraffic.videoid,WebTraffic.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service,priority from WebTraffic left join historyview on historyview.videoid=WebTraffic.videoid and running=1 where user=?1 and timestart=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrderPending(String user);

    @Query(value = "Select WebTraffic.orderid,WebTraffic.videoid,WebTraffic.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from WebTraffic left join historyview on historyview.videoid=WebTraffic.videoid and running=1 where user=?1 and WebTraffic.valid=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrderCheckCancel(String user);

    @Query(value = "Select webtraffic.orderid,webtraffic.videoid,webtraffic.videotitle,count(*) as total,maxthreads,insertdate,timestart,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service,priority from WebTraffic left join historyview on historyview.videoid=WebTraffic.videoid and running=1 where WebTraffic.videoid=?1",nativeQuery = true)
    public List<OrderViewRunning> getWebTrafficById(String videoid);

    @Query(value = "SELECT webtraffic.orderid,count(*) as view FROM historytrafficsum left join webtraffic on historytrafficsum.orderid=webtraffic.orderid" +
            " where  time>=webtraffic.insertdate and service in(select service from service where category='Website') and duration>0 and timestart>0 group by webtraffic.orderid order by insertdate desc",nativeQuery = true)
    public List<String> getTotalTrafficBuff();

    @Query(value = "SELECT webtraffic.orderid,count(*) as view FROM historytrafficsum left join webtraffic on historytrafficsum.orderid=webtraffic.orderid where time>=webtraffic.insertdate and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 and duration>0 group by webtraffic.orderid order by insertdate desc",nativeQuery = true)
    public List<String> get24hTrafficBuff();


    @Query(value = "SELECT count(*) FROM WebTraffic where videoid=?1 and service in(select service from service where geo=?2)",nativeQuery = true)
    public Integer getServiceByVideoId(String videoid,String geo);
    @Modifying
    @Transactional
    @Query(value = "UPDATE webtraffic set viewstart=?1,maxthreads=?2,timestart=?3 where timestart=0 and maxthreads=0 and videoid=?4",nativeQuery = true)
    public void updatePendingOrderByVideoId(Integer viewstart,Integer maxthreads,Long timestart,String videoid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE webtraffic set traffictotal=?1,timeupdate=?2,traffic24h=?3 where orderid=?4",nativeQuery = true)
    public void updateTrafficOrderByOrderId(Integer traffictotal,Long timeupdate,Integer traffic24h,Long orderid);

    @Modifying
    @Transactional
    @Query(value = "update WebTraffic set valid=0 where videoid not in (select videoid from historyviewsum where round((UNIX_TIMESTAMP()-time/1000)/60)<=5  group by videoid ) and round((UNIX_TIMESTAMP()-insertdate/1000)/60)>20",nativeQuery = true)
    public void updateOrderCheckCancel();

    @Modifying
    @Transactional
    @Query(value = "update WebTraffic set valid=1 where videoid=?1",nativeQuery = true)
    public void updateOrderCheck(String videoid);

    @Query(value = "SELECT * FROM WebTraffic where valid=0 order by insertdate asc",nativeQuery = true)
    public List<WebTraffic> getAllOrderCheckCancel();

    @Modifying
    @Transactional
    @Query(value = "update WebTraffic set valid=0 where videoid=?1 and valid=1",nativeQuery = true)
    public void updateCheckCancel(String videoid);

    @Modifying
    @Transactional
    @Query(value = "update WebTraffic set valid=1 where  valid=0",nativeQuery = true)
    public void updateCheckCancelDone();

    @Query(value = "SELECT sum(vieworder) as total FROM webTraffic",nativeQuery = true)
    public Integer getCountViewBuffOrder();


    @Query(value = "SELECT *  FROM WebTraffic where orderid=?1 limit 1",nativeQuery = true)
    public WebTraffic getInfoByOrderId(Long orderid);

    @Query(value = "SELECT sum(vieworder) as total FROM WebTraffic where user=?1",nativeQuery = true)
    public Integer getCountViewBuffOrder(String user);

    @Query(value = "SELECT count(*) as viewbuff FROM historyviewsum left join WebTraffic on historyviewsum.videoid=WebTraffic.videoid where historyviewsum.duration>0 and time>=WebTraffic.insertdate",nativeQuery = true)
    public Integer getCountViewBuffedOrder();

    @Query(value = "SELECT count(*) as viewbuff FROM historyviewsum left join WebTraffic on historyviewsum.videoid=WebTraffic.videoid where historyviewsum.duration>0 and WebTraffic.user=?1  time>=WebTraffic.insertdate",nativeQuery = true)
    public Integer getCountViewBuffedOrder(String user);

    @Query(value = "SELECT * from webtraffic  where orderid=?1 limit 1",nativeQuery = true)
    public List<WebTraffic> getWebTrafficByOrderId(Long orderid);

    @Query(value = "SELECT cast((((SELECT count(*) from account where running=1 and geo='vn')/(select leveluser from setting limit 1))-\n" +
            "            (SELECT sum(threadset) FROM WebTraffic where service in(select service from service where geo='vn' and checktime=0 and timestart!=0)))/2000 as SIGNED)",nativeQuery = true)
    public Integer getMaxRunningBuffHVN();

    @Query(value = "SELECT cast((((SELECT count(*) from account where running=1 and geo='us')/(select leveluser from setting limit 1))-\n" +
            "            (SELECT sum(threadset) FROM WebTraffic where service in(select service from service where geo='us' and checktime=0 and timestart!=0)))/2000 as SIGNED)",nativeQuery = true)
    public Integer getMaxRunningBuffHUS();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM webtraffic where orderid=?1",nativeQuery = true)
    public void deletevideoByOrderId(Long orderid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM WebTraffic where videoid=?1 and user='baohanh01@gmail.com'",nativeQuery = true)
    public void deletevideoByVideoIdBH(String videoid);

    @Modifying
    @Transactional
    @Query(value = "update WebTraffic set maxthreads=maxthreads+cast(threadset*0.16 as UNSIGNED) where service in (select service from service where maxtime<=5) and maxthreads<threadset and maxthreads>0 and timestart>0;",nativeQuery = true)
    public void updateThreadByThreadSet5m();
    @Modifying
    @Transactional
    @Query(value = "update WebTraffic set maxthreads=maxthreads+cast(threadset*0.09 as UNSIGNED) where service in (select service from service where maxtime=10) and maxthreads<threadset and maxthreads>0 and timestart>0;",nativeQuery = true)
    public void updateThreadByThreadSet10m();

    @Modifying
    @Transactional
    @Query(value = "update WebTraffic set maxthreads=maxthreads+cast(threadset*0.06 as UNSIGNED) where service in (select service from service where maxtime=15) and maxthreads<threadset and maxthreads>0 and timestart>0;",nativeQuery = true)
    public void updateThreadByThreadSet15m();

    @Modifying
    @Transactional
    @Query(value = "update WebTraffic set maxthreads=maxthreads+cast(threadset*0.05 as UNSIGNED) where service in (select service from service where maxtime=20) and maxthreads<threadset and maxthreads>0 and timestart>0;",nativeQuery = true)
    public void updateThreadByThreadSet20m();

    @Modifying
    @Transactional
    @Query(value = "update WebTraffic set maxthreads=maxthreads+cast(threadset*0.03 as UNSIGNED) where service in (select service from service where maxtime>=30) and maxthreads<threadset and maxthreads>0 and timestart>0;",nativeQuery = true)
    public void updateThreadByThreadSet30m();
    @Query(value = "select * from webtraffic where traffictotal>(trafficorder + trafficorder*(select bonus/100 from setting where id=1)) and service in(select service from service where category='Website')",nativeQuery = true)
    public List<WebTraffic> getOrderFullTraffic();

    @Query(value = "select * from WebTraffic where viewtotal>(vieworder + vieworder*(select bonus/100 from setting where id=1)) and service in(select service from service where live=1) and round((UNIX_TIMESTAMP()-timestart/1000)/60)>=(minstart+5)",nativeQuery = true)
    public List<WebTraffic> getOrderFullLive();

    @Query(value = "select * from WebTraffic where timetotal>(1800*vieworder + 1800*vieworder*(select bonus/100 from setting where id=1))  and viewtotal>(vieworder + vieworder*(select bonus/100 from setting where id=1)) and service in(select service from service where mintime=120 and checktime=1)",nativeQuery = true)
    public List<WebTraffic> getOrderFullTime120m();
    @Query(value = "select * from WebTraffic where timetotal>(1800*vieworder + 1800*vieworder*(select bonus/100 from setting where id=1))  and viewtotal>(vieworder + vieworder*(select bonus/100 from setting where id=1)) and service in(select service from service where mintime=60 and checktime=1)",nativeQuery = true)
    public List<WebTraffic> getOrderFullTime60m();
    @Query(value = "select * from WebTraffic where timetotal>(1800*vieworder + 1800*vieworder*(select bonus/100 from setting where id=1))  and viewtotal>(vieworder + vieworder*(select bonus/100 from setting where id=1)) and service in(select service from service where mintime=30 and checktime=1)",nativeQuery = true)
    public List<WebTraffic> getOrderFullTime30m();

    @Query(value = "select * from WebTraffic where timetotal>(900*vieworder + 900*vieworder*(select bonus/100 from setting where id=1))  and viewtotal>(vieworder + vieworder*(select bonus/100 from setting where id=1)) and service in(select service from service where mintime=15 and checktime=1)",nativeQuery = true)
    public List<WebTraffic> getOrderFullTime15m();

    @Query(value = "select * from WebTraffic where timetotal>(600*vieworder + 600*vieworder*(select bonus/100 from setting where id=1))  and viewtotal>(vieworder + vieworder*(select bonus/100 from setting where id=1)) and service in(select service from service where maxtime=10 and checktime=1)",nativeQuery = true)
    public List<WebTraffic> getOrderFullTime10m();


    @Query(value = "SELECT WebTraffic.videoid,sum(historyviewsum.duration) as total,count(*) as view FROM historyviewsum left join WebTraffic on historyviewsum.videoid=WebTraffic.videoid where  time>=WebTraffic.insertdate and service in(select service from service where checktime=1) and timestart>0 group by WebTraffic.videoid order by insertdate desc",nativeQuery = true)
    public List<String> getTimeBuffVideo();

    @Query(value = "SELECT sum(maxthreads) FROM AccPremium.WebTraffic where service in(select service from service where live=1)",nativeQuery = true)
    public Integer getSumThreadLive();

    @Modifying
    @Transactional
    @Query(value = "UPDATE WebTraffic set timetotal=?1,viewtotal=?2,timeupdate=?3 where videoid=?4",nativeQuery = true)
    public void updateTimeViewOrderByVideoId(Integer timetotal,Integer viewtotal,Long timeupdate, String videoid);
    @Query(value = "call speedup_threads()",nativeQuery = true)
    public Integer speedup_threads();
}
