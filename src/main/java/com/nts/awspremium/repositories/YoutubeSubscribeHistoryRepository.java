package com.nts.awspremium.repositories;

import com.nts.awspremium.model.YoutubeSubscribeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface YoutubeSubscribeHistoryRepository extends JpaRepository<YoutubeSubscribeHistory,Long> {
    @Query(value = "SELECT list_id FROM youtube_channel_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_ChannelId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM youtube_channel_history where account_id=?1 limit 1",nativeQuery = true)
    public YoutubeSubscribeHistory get_By_AccountId(String account_id);
}
