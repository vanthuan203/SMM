package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistorySum;
import com.nts.awspremium.model.YoutubeViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HistorySumRepository extends JpaRepository<HistorySum,Long> {
    @Query(value = "SELECT count(*) FROM history_sum where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_VideoId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM youtube_video_history where account_id=?1 limit 1",nativeQuery = true)
    public YoutubeViewHistory get_By_AccountId(String account_id);
}
