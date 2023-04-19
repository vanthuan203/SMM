package com.nts.awspremium.repositories;


import com.nts.awspremium.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoViewRepository extends JpaRepository<VideoView,Long> {

    @Query(value = "SELECT * FROM videoview where service not in (999,998) and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (select orderid from (select videoview.orderid,count(*) as total,maxthreads\n" +
            "            from videoview left join historyview on historyview.orderid=videoview.orderid and running=1\n" +
            "            group by orderid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2NoCheckTime24hNoTest(String listvideo);
    @Query(value = "SELECT * FROM videoview where service not in (999,998) and service>600 and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (select orderid from (select videoview.orderid,count(*) as total,maxthreads\n" +
            "            from videoview left join historyview on historyview.orderid=videoview.orderid and running=1\n" +
            "            group by orderid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2VN(String listvideo);

    @Query(value = "SELECT * FROM videoview where service not in (999,998) and service>600 and INSTR(?1,videoid)=0 and  orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2VNTEST(String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service not in (999,998) and service<600 and INSTR(?1,videoid)=0 and  orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2USTEST(String listvideo, List<String> orderid);

    @Query(value = "SELECT * FROM videoview where service not in (999,998) and service<500 and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (select orderid from (select videoview.orderid,count(*) as total,maxthreads\n" +
            "            from videoview left join historyview on historyview.orderid=videoview.orderid and running=1\n" +
            "            group by orderid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2US(String listvideo);
    @Query(value = "SELECT * FROM videoview where service not in (999,998) and service>600 and INSTR(?1,videoid)=0  order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewNoCheckMaxThreadVN(String listvideo);

    @Query(value = "SELECT * FROM videoview where service not in (999,998) and service<500 and INSTR(?1,videoid)=0  order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewNoCheckMaxThreadUS(String listvideo);

    @Query(value = "SELECT * FROM videoview where service=600 and INSTR(?1,videoid)=0  order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewNoCheckMaxThreadVNTEST(String listvideo);

    @Query(value = "SELECT * FROM videoview where service=600 and (CHAR_LENGTH(?1) - CHAR_LENGTH(REPLACE(?1, videoid, ''))) / CHAR_LENGTH(videoid)<=7 and ((char_length(?1) -  LOCATE(REVERSE(videoid),REVERSE(?1))+500)<=char_length(?1) or INSTR(?1,videoid)=0)  order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewLoopNoCheckMaxThreadVNTEST(String listvideo);
    @Query(value = "SELECT * FROM videoview where service=599 and INSTR(?1,videoid)=0  order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewNoCheckMaxThreadUSTEST(String listvideo);

    @Query(value = "SELECT * FROM videoview where service=599 and (CHAR_LENGTH(?1) - CHAR_LENGTH(REPLACE(?1, videoid, ''))) / CHAR_LENGTH(videoid)<=7 and ((char_length(?1) -  LOCATE(REVERSE(videoid),REVERSE(?1))+500)<=char_length(?1) or INSTR(?1,videoid)=0)  order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewLoopNoCheckMaxThreadUSTEST(String listvideo);

    @Query(value = "SELECT * FROM videoview where service not in (999,998) and service>600 and (CHAR_LENGTH(?1) - CHAR_LENGTH(REPLACE(?1, videoid, ''))) / CHAR_LENGTH(videoid)<=7 and ((char_length(?1) -  LOCATE(REVERSE(videoid),REVERSE(?1))+500)<=char_length(?1) or INSTR(?1,videoid)=0)  order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewLoopNoCheckMaxThreadVN(String listvideo);

    @Query(value = "SELECT * FROM videoview where service not in (999,998) and service<500 and (CHAR_LENGTH(?1) - CHAR_LENGTH(REPLACE(?1, videoid, ''))) / CHAR_LENGTH(videoid)<=7 and ((char_length(?1) -  LOCATE(REVERSE(videoid),REVERSE(?1))+500)<=char_length(?1) or INSTR(?1,videoid)=0)  order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewLoopNoCheckMaxThreadUS(String listvideo);

    @Query(value = "SELECT * FROM videoview where service not in (999,998) and service>600 and" +
            " (CHAR_LENGTH(?1) - CHAR_LENGTH(REPLACE(?1, videoid, ''))) / CHAR_LENGTH(videoid)<=7 and ((char_length(?1) -  LOCATE(REVERSE(videoid),REVERSE(?1))+500)<=char_length(?1) or INSTR(?1,videoid)=0) and\n" +
            "            orderid in (select orderid from (select videoview.orderid,count(*) as total,maxthreads\n" +
            "            from videoview left join historyview on historyview.orderid=videoview.orderid and running=1\n" +
            "            group by orderid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewLoopVer2VN(String listvideo);

    @Query(value = "select orderid from (select videoview.orderid,count(*) as total,maxthreads\n" +
            "             from videoview left join historyview on historyview.orderid=videoview.orderid and running=1\n" +
            "             group by orderid having total<maxthreads) as t",nativeQuery = true)
    public List<String> getListOrderTrueThread();

    @Query(value = "SELECT * FROM videoview where service not in (999,998) and service<500 and" +
            " (CHAR_LENGTH(?1) - CHAR_LENGTH(REPLACE(?1, videoid, ''))) / CHAR_LENGTH(videoid)<=7 and ((char_length(?1) -  LOCATE(REVERSE(videoid),REVERSE(?1))+500)<=char_length(?1) or INSTR(?1,videoid)=0) and\n" +
            "            orderid in (select orderid from (select videoview.orderid,count(*) as total,maxthreads\n" +
            "            from videoview left join historyview on historyview.orderid=videoview.orderid and running=1\n" +
            "            group by orderid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewLoopVer2US(String listvideo);

    @Query(value = "SELECT * FROM videoview where service in (999,998) and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (select orderid from (select videoview.orderid,count(*) as total,maxthreads\n" +
            "            from videoview left join historyview on historyview.orderid=videoview.orderid and running=1\n" +
            "            group by orderid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2NoCheckTime24hNoTestTimeBuff(String listvideo);

    @Query(value = "SELECT * FROM videoview where service in (999,998) and INSTR(?1,videoid)=0  order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewNoCheckMaxThreadViewBuff(String listvideo);

    @Query(value = "SELECT * FROM videoview where service in (999,998) and (CHAR_LENGTH(?1) - CHAR_LENGTH(REPLACE(?1, videoid, ''))) / CHAR_LENGTH(videoid)<=7 and ((char_length(?1) -  LOCATE(REVERSE(videoid),REVERSE(?1))+500)<=char_length(?1) or INSTR(?1,videoid)=0)  order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewLoopNoCheckMaxThreadViewBuff(String listvideo);


    @Query(value = "SELECT * from videoview where orderid in (?1)",nativeQuery = true)
    public List<VideoView> getVideoViewByListId(List<String> list_orderid);
    @Query(value = "SELECT * from videoview where orderid=?1",nativeQuery = true)
    public VideoView getVideoViewById(Long orderid);

    @Query(value = "SELECT * from videoview where videoid=?1",nativeQuery = true)
    public VideoView getVideoViewByVideoid(String videoid);


    @Query(value = "SELECT count(*) from videoview where user=?1",nativeQuery = true)
    public Integer getCountOrderByUser(String user);

    @Query(value = "SELECT count(*) from videoview where videoid=?1",nativeQuery = true)
    public Integer getCountVideoId(String videoid);

    @Query(value = "SELECT * FROM videoview order by timeupdate asc",nativeQuery = true)
    public List<VideoView> getAllOrder();

    @Query(value = "SELECT * FROM videoview where maxthreads=0 order by insertdate asc",nativeQuery = true)
    public List<VideoView> getAllOrderPending();

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where user!='baohanh01@gmail.com' group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrder();

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where videoview.valid=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrderCheckCancel();

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where user=?1 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrder(String user);

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where user=?1 and videoview.valid=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrderCheckCancel(String user);

    @Query(value = "Select videoview.orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where videoview.videoid=?1",nativeQuery = true)
    public List<OrderViewRunning> getVideoViewById(String videoid);

    @Query(value = "SELECT videoview.videoid,count(*) as view FROM historyviewsum left join videoview on historyviewsum.videoid=videoview.videoid where  time>=videoview.insertdate group by videoview.videoid order by insertdate desc",nativeQuery = true)
    public List<String> getTotalViewBuff();

    @Query(value = "SELECT videoview.videoid,count(*) as view FROM historyviewsum left join videoview on historyviewsum.videoid=videoview.videoid where time>=videoview.insertdate and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by videoview.videoid order by insertdate desc",nativeQuery = true)
    public List<String> get24hViewBuff();

    @Modifying
    @Transactional
    @Query(value = "UPDATE videoview set viewstart=?1,maxthreads=?2,timeupdate=?3 where videoid=?4",nativeQuery = true)
    public void updatePendingOrderByVideoId(Integer viewstart,Integer maxthreads,Long timeupdate,String videoid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE videoview set viewtotal=?1,view24h=?2,timeupdate=?3 where videoid=?4",nativeQuery = true)
    public void updateViewOrderByVideoId(Integer viewtotal,Integer view24h,Long timeupdate,String videoid);

    @Modifying
    @Transactional
    @Query(value = "update videoview set valid=0 where videoid not in (select videoid from historyviewsum where round((UNIX_TIMESTAMP()-time/1000)/60)<=5  group by videoid ) and round((UNIX_TIMESTAMP()-insertdate/1000)/60)>20",nativeQuery = true)
    public void updateOrderCheckCancel();

    @Modifying
    @Transactional
    @Query(value = "update videoview set valid=1 where videoid=?1",nativeQuery = true)
    public void updateOrderCheck(String videoid);

    @Modifying
    @Transactional
    @Query(value = "update videoview set valid=0 where videoid=?1 and valid=1",nativeQuery = true)
    public void updateCheckCancel(String videoid);

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


    @Query(value = "select * from videoview where viewtotal>(vieworder + vieworder*(select bonus/100 from setting where id=1))",nativeQuery = true)
    public List<VideoView> getOrderFullView();

}
