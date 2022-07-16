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
    @Query(value = "Select channel.channelid,channel.title,count(*) as total,maxthreads from channel left join history on history.channelid=channel.channelid and running=1   group by channelid order by total desc",nativeQuery = true)
    public List<OrderRunning> getOrderRunning();
    @Query(value = "Select channel.channelid,channel.title,count(*) as total,maxthreads,viewpercent,insertdate,enabled from channel left join history on history.channelid=channel.channelid and running=1   group by channelid",nativeQuery = true)
    public List<OrderRunning> getOrder();
}
