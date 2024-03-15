package com.nts.awspremium.repositories;


import com.nts.awspremium.model.ChannelTiktok;
import com.nts.awspremium.model.OrderTrafficRunning;
import com.nts.awspremium.model.WebTraffic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ChannelTikTokRepository extends JpaRepository<ChannelTiktok,Long> {

    @Query(value = "SELECT * FROM channel_tiktok where INSTR(?1,tiktok_id)=0 order by rand() limit 1",nativeQuery = true)
    public List<ChannelTiktok> getChannelTiktokBy(String list_tiktok_id);

    @Query(value = "select orderid from (select webtraffic.orderid,count(running) as total,maxthreads,valid,traffictotal,trafficorder,speedup,traffic24h,maxtraffic24h\n" +
            "                                from webtraffic left join historytraffic on historytraffic.orderid=webtraffic.orderid and running=1 where !(maxthreads=1 and 1440*0.3/maxtraffic24h>=round((UNIX_TIMESTAMP()-lastcompleted/1000)/60))\n" +
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

    @Query(value = "SELECT orderid from webtraffic where link=?1 limit 1",nativeQuery = true)
    public Long getOrderIdWebTrafficByLink(String link);





    @Query(value = "SELECT count(*) from webtraffic where link=?1",nativeQuery = true)
    public Integer getCountLink(String link);

    @Query(value = "SELECT * FROM  webtraffic where service in(select service from service where category='Website') and timestart>0 order by timeupdate asc",nativeQuery = true)
    public List<WebTraffic> getAllOrderTraffic();



    @Query(value = "Select webtraffic.orderid,webtraffic.link,count(running) as total,maxthreads,insertdate,timestart,note,trafficorder,user,traffictotal,timeupdate,traffic24h,price,service,keywords from webtraffic left join historytraffic on historytraffic.orderid=webtraffic.orderid and running=1 where user!='baohanh01@gmail.com' and timestart!=0 group by orderid order by insertdate desc",nativeQuery = true)
    public List<OrderTrafficRunning> getOrder();

    @Query(value = "Select webtraffic.orderid,webtraffic.link,count(running) as total,maxthreads,insertdate,timestart,note,trafficorder,user,traffictotal,timeupdate,traffic24h,price,service,keywords from webtraffic left join historytraffic on historytraffic.orderid=webtraffic.orderid and running=1 where user=?1 and timestart!=0 group by orderid order by insertdate desc",nativeQuery = true)
    public List<OrderTrafficRunning> getOrder(String user);


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
    @Query(value = "update webtraffic set valid=0 where orderid=?1 and valid=1",nativeQuery = true)
    public void updateCheckCancel(Long orderid);



    @Query(value = "SELECT * from webtraffic  where orderid=?1 limit 1",nativeQuery = true)
    public List<WebTraffic> getWebTrafficByOrderId(Long orderid);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM webtraffic where orderid=?1",nativeQuery = true)
    public void deletevideoByOrderId(Long orderid);


    @Query(value = "select * from webtraffic where traffictotal>(trafficorder + trafficorder*(select bonus/100 from setting where id=1)) and service in(select service from service where category='Website')",nativeQuery = true)
    public List<WebTraffic> getOrderFullTraffic();

}
