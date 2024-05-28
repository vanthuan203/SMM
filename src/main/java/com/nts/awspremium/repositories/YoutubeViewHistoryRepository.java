package com.nts.awspremium.repositories;

import com.nts.awspremium.model.YoutubeViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface YoutubeViewHistoryRepository extends JpaRepository<YoutubeViewHistory,Long> {
    @Query(value = "SELECT list_id FROM youtube_view_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_VideoId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM youtube_view_history where account_id=?1 limit 1",nativeQuery = true)
    public YoutubeViewHistory get_By_AccountId(String account_id);
}
