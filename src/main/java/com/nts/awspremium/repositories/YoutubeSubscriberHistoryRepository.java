package com.nts.awspremium.repositories;

import com.nts.awspremium.model.YoutubeSubscriberHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface YoutubeSubscriberHistoryRepository extends JpaRepository<YoutubeSubscriberHistory,Long> {
    @Query(value = "SELECT list_id FROM youtube_subscriber_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_ChannelId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM youtube_subscriber_history where account_id=?1 limit 1",nativeQuery = true)
    public YoutubeSubscriberHistory get_By_AccountId(String account_id);
}
