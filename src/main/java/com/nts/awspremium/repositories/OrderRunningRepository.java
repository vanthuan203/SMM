package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Channel;
import com.nts.awspremium.model.OrderRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

public interface OrderRunningRepository extends JpaRepository<Channel,Long> {
    @Query(value = "Select channel.channelid,channel.title,count(*) as total,maxthreads from channel left join history on history.channelid=channel.channelid and running=1 group by channelid order by total desc",nativeQuery = true)
    public List<OrderRunning> getOrderRunning();

    @Query(value = "Select channel.channelid,channel.title,count(*) as total,maxthreads from channel left join history on history.channelid=channel.channelid and running=1 group by channelid order by total desc",nativeQuery = true)
    public List<OrderRunning> getOrderRunningBuffh();
    @Query(value = "Select channel.channelid,channel.title,count(*) as total,maxthreads,viewpercent,insertdate,enabled from channel left join history on history.channelid=channel.channelid and running=1 group by channelid order by insertdate desc",nativeQuery = true)
    public List<OrderRunning> getOrder();

    @Query(value = "Select channel.channelid,channel.title,count(*) as total,maxthreads,viewpercent,insertdate,enabled from channel left join history on history.channelid=channel.channelid and running=1 where enabled=0   group by channelid order by insertdate desc",nativeQuery = true)
    public List<OrderRunning> getOrderDone();

    @Query(value = "select c.channelid,c.title,0,c.maxthreads,c.viewpercent,c.insertdate,c.enabled from channel c left join historysum h on c.channelid=h.channelid where enabled!=0 group by c.channelid having sum(duration)>3600*c.viewpercent",nativeQuery = true)
    public List<OrderRunning> getOrderFullBuffh();
    @Query(value = "Select channel.channelid,channel.title,count(*) as total,maxthreads,viewpercent,insertdate,enabled from channel left join history on history.channelid=channel.channelid and running=1 where channel.channelid=?1  group by channelid",nativeQuery = true)
    public List<OrderRunning> getOrderByChannelid(String channelid);
}
