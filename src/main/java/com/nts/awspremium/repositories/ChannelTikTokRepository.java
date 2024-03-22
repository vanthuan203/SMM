package com.nts.awspremium.repositories;


import com.nts.awspremium.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ChannelTikTokRepository extends JpaRepository<ChannelTiktok,Long> {

    @Query(value = "SELECT count(*) from channel_tiktok where service=?1",nativeQuery = true)
    public Integer getCountOrderByService(Integer service);

    @Query(value = "SELECT * FROM channel_tiktok where service in(select service from service where category='Tiktok') and INSTR(?1,CONCAT(tiktok_id,'|'))=0 and orderid in (?2) order by rand() limit 1",nativeQuery = true)
    public List<ChannelTiktok> getChannelTiktokByTask(String list_tiktok_id, List<String> orderid);

    @Query(value = "select orderid from (select channel_tiktok.orderid,count(running) as total,max_threads\n" +
            "                      from channel_tiktok left join history_tiktok on history_tiktok.orderid=channel_tiktok.orderid and running=1\n" +
            "                       group by orderid having total<max_threads) as t",nativeQuery = true)
    public List<String> getListOrderTrueThreadON();

    @Query(value = "SELECT count(*) from channel_tiktok where tiktok_id=?1",nativeQuery = true)
    public Integer getCountTiktokId(String tiktok_id);

    @Query(value = "SELECT count(*) from channel_tiktok where orderid=?1 and token=?2 ",nativeQuery = true)
    public Integer checkTrueByOrderIdAndToken(Long orderid,String token);

    @Query(value = "SELECT * from channel_tiktok where orderid in (?1)",nativeQuery = true)
    public List<ChannelTiktok> getChannelTiktokByListId(List<String> list_orderid);
    @Query(value = "SELECT * from channel_tiktok where orderid=?1",nativeQuery = true)
    public ChannelTiktok getChannelTiktokById(Long orderid);

    @Query(value = "SELECT orderid from webtraffic where link=?1 limit 1",nativeQuery = true)
    public Long getOrderIdWebTrafficByLink(String link);





    @Query(value = "SELECT count(*) from channel_tiktok where tiktok_id=?1",nativeQuery = true)
    public Integer getCountLink(String link);

    @Query(value = "SELECT * FROM  channel_tiktok where service in(select service from service where category='Website') and timestart>0 order by timeupdate asc",nativeQuery = true)
    public List<ChannelTiktok> getAllOrderTraffic();



    @Query(value = "Select channel_tiktok.orderid,channel_tiktok.tiktok_id,count(running) as total,max_threads,insert_date,time_start,note,follower_order,user,follower_total,price,service,follower_start from channel_tiktok left join history_tiktok on history_tiktok.orderid=channel_tiktok.orderid and running=1 where user!='baohanh01@gmail.com' and time_start!=0 group by orderid order by insert_date desc",nativeQuery = true)
    public List<OrderFollowerTikTokRunning> getOrder();

    @Query(value = "Select channel_tiktok.orderid,channel_tiktok.tiktok_id,count(running) as total,max_threads,insert_date,time_start,note,follower_order,user,follower_total,price,service,follower_start from channel_tiktok left join history_tiktok on history_tiktok.orderid=channel_tiktok.orderid and running=1 where tiktok_id=?1",nativeQuery = true)
    public List<OrderFollowerTikTokRunning> getOrderByTiktokId(String tiktok_id);

    @Query(value = "Select channel_tiktok.orderid,channel_tiktok.tiktok_id,count(running) as total,max_threads,insert_date,time_start,note,follower_order,user,follower_total,price,service,follower_start from channel_tiktok left join history_tiktok on history_tiktok.orderid=channel_tiktok.orderid and running=1 where user=?1 and time_start!=0 group by orderid order by insert_date desc",nativeQuery = true)
    public List<OrderFollowerTikTokRunning> getOrder(String user);


    @Query(value = "SELECT webtraffic.orderid,count(*) as view FROM historytrafficsum left join webtraffic on historytrafficsum.orderid=webtraffic.orderid" +
            " where  time>=webtraffic.insertdate and service in(select service from service where category='Website') and duration>0 and timestart>0 group by webtraffic.orderid order by insertdate desc",nativeQuery = true)
    public List<String> getTotalTrafficBuff();

    @Query(value = "SELECT webtraffic.orderid,count(*) as view FROM historytrafficsum left join webtraffic on historytrafficsum.orderid=webtraffic.orderid where time>=webtraffic.insertdate and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 and duration>0 group by webtraffic.orderid order by insertdate desc",nativeQuery = true)
    public List<String> get24hTrafficBuff();

    @Modifying
    @Transactional
    @Query(value = "UPDATE webtraffic set traffictotal=?1,timeupdate=?2,traffic24h=?3 where orderid=?4",nativeQuery = true)
    public void updateTrafficOrderByOrderId(Integer traffictotal,Long timeupdate,Integer traffic24h,Long orderid);




    @Modifying
    @Transactional
    @Query(value = "update channel_tiktok set valid=0 where orderid=?1 and valid=1",nativeQuery = true)
    public void updateCheckCancel(Long orderid);



    @Query(value = "SELECT * from channel_tiktok  where tiktok_id=?1 limit 1",nativeQuery = true)
    public List<ChannelTiktok> getChannelTiktokByTiktokId(String tiktok_id);



    @Modifying
    @Transactional
    @Query(value = "DELETE FROM channel_tiktok where orderid=?1",nativeQuery = true)
    public void deletevideoByOrderId(Long orderid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM channel_tiktok where tiktok_id=?1",nativeQuery = true)
    public void deleteByTiktokId(String tiktok_id);


    @Query(value = "select * from webtraffic where traffictotal>(trafficorder + trafficorder*(select bonus/100 from setting where id=1)) and service in(select service from service where category='Website')",nativeQuery = true)
    public List<WebTraffic> getOrderFullTraffic();

}
