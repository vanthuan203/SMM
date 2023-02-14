package com.nts.awspremium.repositories;


import com.nts.awspremium.model.OrderBuffhRunning;
import com.nts.awspremium.model.OrderViewRunning;
import com.nts.awspremium.model.VideoBuffh;
import com.nts.awspremium.model.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoViewRepository extends JpaRepository<VideoView,Long> {

    @Query(value = "SELECT * FROM videoview where INSTR(?1,videoid)=0 and \n" +
            "videoid in (select videoid from (select videoview.videoid,count(*) as total,maxthreads \n" +
            "from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 \n" +
            "group by videoid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoView> getvideoViewVer2NoCheckTime24hNoTest(String listvideo);
    @Query(value = "SELECT * from videoview where orderid in (?1)",nativeQuery = true)
    public List<VideoView> getVideoViewByListId(List<String> list_orderid);

    @Query(value = "SELECT count(*) from videoview where user=?1",nativeQuery = true)
    public Integer getCountOrderByUser(String user);

    @Query(value = "SELECT count(*) from videoview where videoid=?1",nativeQuery = true)
    public Integer getCountVideoId(String videoid);

    @Query(value = "SELECT * FROM videoview order by timeupdate asc",nativeQuery = true)
    public List<VideoView> getAllOrder();

    @Query(value = "Select orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrder();

    @Query(value = "Select orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where user=?1 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderViewRunning> getOrder(String user);
    @Query(value = "Select orderid,videoview.videoid,videoview.videotitle,count(*) as total,maxthreads,insertdate,note,duration,viewstart,vieworder,user,viewtotal,timeupdate,view24h,price,service from videoview left join historyview on historyview.videoid=videoview.videoid and running=1 where videoview.videoid=?1",nativeQuery = true)
    public List<OrderViewRunning> getVideoViewById(String videoid);

    @Query(value = "SELECT videoview.videoid,count(*) as view FROM historyviewsum left join videoview on historyviewsum.videoid=videoview.videoid where  time>=videoview.insertdate group by videoview.videoid order by insertdate desc",nativeQuery = true)
    public List<String> getTotalViewBuff();

    @Query(value = "SELECT videoview.videoid,count(*) as view FROM historyviewsum left join videoview on historyviewsum.videoid=videoview.videoid where time>=videoview.insertdate and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by videoview.videoid order by insertdate desc",nativeQuery = true)
    public List<String> get24hViewBuff();


    @Modifying
    @Transactional
    @Query(value = "UPDATE videoview set viewtotal=?1,view24h=?2,timeupdate=?3 where videoid=?4",nativeQuery = true)
    public void updateViewOrderByVideoId(Integer viewtotal,Integer view24h,Long timeupdate,String videoid);

    @Query(value = "SELECT sum(vieworder) as total FROM videoview",nativeQuery = true)
    public Integer getCountViewBuffOrder();

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
