package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ChannelBlackList;
import com.nts.awspremium.model.HistoryFollower24hTikTok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface ChannelYoutubeBlackListRepository extends JpaRepository<ChannelBlackList,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from channel_youtube_blacklist",nativeQuery = true)
    public void deleteAll();
    @Query(value = "select count(*) from channel_youtube_blacklist where channel_id=?1",nativeQuery = true)
    public Integer getCountByChannelId(String channel_id);
}
