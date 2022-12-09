package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Channel;
import com.nts.awspremium.model.OrderRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel,Long> {
    @Query(value = "SELECT * FROM channel WHERE channelid=?1",nativeQuery = true)
    public List<Channel> getChannelById(String channelid);

    @Query(value = "SELECT channelid,sum(duration) as total,count(*) as view FROM AccPremium.historysum where channelid in (select channel.channelid from channel) group by channelid",nativeQuery = true)
    public List<String> getTimeBuffChannel();
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM channel WHERE channelid=?1",nativeQuery = true)
    public void deleteChannelById(String channelid);

}
