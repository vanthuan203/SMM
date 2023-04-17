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

    @Query(value = "SELECT * FROM videocomment where service=222 and INSTR(?1,videoid)=0 and\n" +
            "            orderid in (select orderid from (select videocomment.orderid,count(*) as total,maxthreads\n" +
            "            from videocomment left join historycomment on historycomment.orderid=videocomment.orderid and running=1\n" +
            "            group by orderid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<VideoComment> getvideoCommentUS(String listvideo);

    @Query(value = "SELECT * from videocomment where orderid in (?1)",nativeQuery = true)
    public List<VideoComment> getVideoViewByListId(List<String> list_orderid);
    @Query(value = "SELECT * from videocomment where orderid=?1",nativeQuery = true)
    public VideoComment getVideoViewById(Long orderid);

    @Query(value = "SELECT orderid from videocomment where videoid=?1",nativeQuery = true)
    public Long getOrderIdByVideoId(String videoid);


    @Query(value = "SELECT count(*) from videoview where user=?1",nativeQuery = true)
    public Integer getCountOrderByUser(String user);

    @Query(value = "SELECT count(*) from videocomment where videoid=?1",nativeQuery = true)
    public Integer getCountVideoId(String videoid);

    @Query(value = "SELECT * FROM videocomment order by timeupdate asc",nativeQuery = true)
    public List<VideoComment> getAllOrder();

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(*) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderCommentRunning> getOrder();

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(*) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,view24h,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 where videoview.valid=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderCommentRunning> getOrderCheckCancel();

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(*) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 where user=?1 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderCommentRunning> getOrder(String user);

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(*) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,view24h,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 where user=?1 and videoview.valid=0 group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderCommentRunning> getOrderCheckCancel(String user);

    @Query(value = "Select videocomment.orderid,videocomment.videoid,videocomment.videotitle,count(*) as total,maxthreads,insertdate,note,duration,commentstart,commentorder,user,commenttotal,timeupdate,view24h,price,service from videocomment left join historycomment on historycomment.videoid=videocomment.videoid and running=1 where videoview.videoid=?1",nativeQuery = true)
    public List<OrderCommentRunning> getVideoViewById(String videoid);

    @Query(value = "SELECT videocomment.videoid,count(*) as view FROM historycommentsum left join videocomment on historycommentsum.orderid=videocomment.orderid where  time>=videocomment.insertdate group by videocomment.orderid order by insertdate desc",nativeQuery = true)
    public List<String> getTotalCommentBuff();

    @Query(value = "SELECT videocomment.videoid,count(*) as view FROM historycommentsum left join videocomment on historycommentsum.videoid=videocomment.videoid where time>=videocomment.insertdate and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by videocomment.videoid order by insertdate desc",nativeQuery = true)
    public List<String> get24hViewBuff();

    @Modifying
    @Transactional
    @Query(value = "UPDATE videocomment set commenttotal=?1,timeupdate=?2 where videoid=?3",nativeQuery = true)
    public void updateViewOrderByVideoId(Integer viewtotal,Long timeupdate,String videoid);

    @Modifying
    @Transactional
    @Query(value = "update videocomment set valid=0 where videoid not in (select videoid from historyviewsum where round((UNIX_TIMESTAMP()-time/1000)/60)<=5  group by videoid ) and round((UNIX_TIMESTAMP()-insertdate/1000)/60)>20",nativeQuery = true)
    public void updateOrderCheckCancel();

    @Modifying
    @Transactional
    @Query(value = "update videocomment set valid=1 where videoid=?1",nativeQuery = true)
    public void updateOrderCheck(String videoid);

    @Query(value = "SELECT sum(vieworder) as total FROM videocomment",nativeQuery = true)
    public Integer getCountViewBuffOrder();

    @Query(value = "SELECT *  FROM videocomment where orderid=?1 limit 1",nativeQuery = true)
    public VideoComment getInfoByOrderId(Long orderid);

    @Query(value = "SELECT sum(vieworder) as total FROM videocomment where user=?1",nativeQuery = true)
    public Integer getCountViewBuffOrder(String user);

    @Query(value = "SELECT count(*) as viewbuff FROM historyviewsum left join videocomment on historyviewsum.videoid=videocomment.videoid where historyviewsum.duration>0 and time>=videocomment.insertdate",nativeQuery = true)
    public Integer getCountViewBuffedOrder();

    @Query(value = "SELECT count(*) as viewbuff FROM historyviewsum left join videocomment on historyviewsum.videoid=videocomment.videoid where historyviewsum.duration>0 and videocomment.user=?1  time>=videocomment.insertdate",nativeQuery = true)
    public Integer getCountViewBuffedOrder(String user);

    @Query(value = "SELECT * from videocomment  where videoid=?1 limit 1",nativeQuery = true)
    public List<VideoComment> getVideoBuffhById(String videoid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM videocomment where videoid=?1",nativeQuery = true)
    public void deletevideoByVideoId(String videoid);


    @Query(value = "select * from videocomment where commenttotal>=commentorder",nativeQuery = true)
    public List<VideoComment> getOrderFullView();

}
