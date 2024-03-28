package com.nts.awspremium.repositories;


import com.nts.awspremium.model.OrderCommentRunning;
import com.nts.awspremium.model.OrderViewRunning;
import com.nts.awspremium.model.VideoComment;
import com.nts.awspremium.model.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoCommentRepository extends JpaRepository<VideoComment,Long> {

    @Query(value = "SELECT * FROM videocomment where service=888 and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (select orderid from (select videocomment.orderid,count(*) as total,maxthreads\n" +
            "            from videocomment left join historycomment on historycomment.orderid=videocomment.orderid and running=1\n" +
            "            group by orderid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoComment> getvideoCommentVN(String listvideo);

    @Query(value = "SELECT * FROM videocomment where service=888 and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoComment> getvideoCommentVN(String listvideo,List<String> orderid);

    @Query(value = "SELECT * FROM videocomment where service=222 and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (select orderid from (select videocomment.orderid,count(*) as total,maxthreads\n" +
            "            from videocomment left join historycomment on historycomment.orderid=videocomment.orderid and running=1\n" +
            "            group by orderid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoComment> getvideoCommentUS(String listvideo);

    @Query(value = "SELECT * FROM videocomment where service=222 and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoComment> getvideoCommentUS(String listvideo,List<String> orderid);

    @Query(value = "SELECT * FROM videocomment where service=1111 and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (select orderid from (select videocomment.orderid,count(*) as total,maxthreads\n" +
            "            from videocomment left join historycomment on historycomment.orderid=videocomment.orderid and running=1\n" +
            "            group by orderid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoComment> getvideoCommentKR(String listvideo);

    @Query(value = "SELECT * FROM videocomment where service=1111 and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<VideoComment> getvideoCommentKR(String listvideo,List<String> orderid);
    @Query(value = "select orderid from (select videocomment.orderid,count(running) as total,maxthreads\n" +
            "                      from videocomment left join historycomment on historycomment.orderid=videocomment.orderid and running=1\n" +
            "                       group by orderid having total<maxthreads) as t",nativeQuery = true)
    public List<String> getListOrderTrueThreadON();

    @Query(value = "SELECT * from videocomment where orderid in (?1)",nativeQuery = true)
    public List<VideoComment> getVideoViewByListId(List<String> list_orderid);
    @Query(value = "SELECT * from videocomment where orderid=?1",nativeQuery = true)
    public VideoComment getVideoViewById(Long orderid);

    @Query(value = "SELECT orderid from videocomment where videoid=?1",nativeQuery = true)
    public Long getOrderIdByVideoId(String videoid);


    @Query(value = "SELECT count(*) from videocomment where user=?1",nativeQuery = true)
    public Integer getCountOrderByUser(String user);

    @Query(value = "SELECT count(*) from videocomment where videoid=?1",nativeQuery = true)
    public Integer getCountVideoId(String videoid);

    @Query(value = "SELECT * FROM videocomment order by timeupdate asc",nativeQuery = true)
    public List<VideoComment> getAllOrder();
    @Query(value = "SELECT count(*) from videocomment where service=?1",nativeQuery = true)
    public Integer getCountOrderByService(Integer service);

    @Modifying
    @Transactional
    @Query(value = "update videocomment set valid=0 where videoid=?1 and valid=1",nativeQuery = true)
    public void updateCheckCancel(String videoid);

    @Query(value = "SELECT * FROM videocomment where valid=0 order by insertdate asc",nativeQuery = true)
    public List<VideoComment> getAllOrderCheckCancel();

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(running) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderCommentRunning> getOrder();

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(running) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 where maxthreads>0 and commenttotal=0 and round((UNIX_TIMESTAMP()-insertdate/1000)/60/60)>2 group by videoid having total>=1 order by insertdate asc",nativeQuery = true)
    public List<OrderCommentRunning> getOrderCancelThan2h();

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(*) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,view24h,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 where videoview.valid=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderCommentRunning> getOrderCheckCancel();

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(running) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 where user=?1 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderCommentRunning> getOrder(String user);

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(*) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,view24h,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 where user=?1 and videoview.valid=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderCommentRunning> getOrderCheckCancel(String user);

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(*) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 where videocomment.videoid=?1",nativeQuery = true)
    public List<OrderCommentRunning> getVideoViewById(String videoid);

    @Query(value = "SELECT videocomment.videoid,count(*) as view FROM datacomment left join videocomment on videocomment.orderid=datacomment.orderid and running=2  group by videocomment.orderid order by insertdate desc",nativeQuery = true)
    public List<String> getTotalCommentBuffByDataComment();



    @Modifying
    @Transactional
    @Query(value = "UPDATE videocomment set commenttotal=?1,timeupdate=?2 where videoid=?3",nativeQuery = true)
    public void updateViewOrderByVideoId(Integer viewtotal,Long timeupdate,String videoid);

    @Query(value = "SELECT count(*) FROM videocomment where videoid=?1 and service in(select service from service where geo=?2)",nativeQuery = true)
    public Integer getServiceByVideoId(String videoid,String geo);

    @Query(value = "SELECT count(*) from videocomment where videoid=?1 and maxthreads>0",nativeQuery = true)
    public Integer getCountVideoIdNotPending(String videoid);

    @Modifying
    @Transactional
    @Query(value = "update videocomment set valid=1 where videoid=?1",nativeQuery = true)
    public void updateOrderCheck(String videoid);


    @Query(value = "SELECT * from videocomment  where videoid=?1 limit 1",nativeQuery = true)
    public List<VideoComment> getVideoBuffhById(String videoid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM videocomment where videoid=?1",nativeQuery = true)
    public void deletevideoByVideoId(String videoid);


    @Query(value = "select * from videocomment where commenttotal>=commentorder",nativeQuery = true)
    public List<VideoComment> getOrderFullView();

    @Query(value = "select * from videocomment where maxthreads=0 order by insertdate asc limit 5",nativeQuery = true)
    public List<VideoComment> getOrderThreadNull();

}
