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
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM channel WHERE channelid=?1",nativeQuery = true)
    public void deleteChannelById(String channelid);

}
