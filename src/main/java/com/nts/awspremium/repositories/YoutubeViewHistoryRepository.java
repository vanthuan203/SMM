package com.nts.awspremium.repositories;

import com.nts.awspremium.model.YoutubeViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface YoutubeViewHistoryRepository extends JpaRepository<YoutubeViewHistory,Long> {
    @Query(value = "SELECT list_id FROM youtube_view_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_VideoId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM youtube_view_history where account_id=?1 limit 1",nativeQuery = true)
    public YoutubeViewHistory get_By_AccountId(String account_id);

    @Modifying
    @Transactional
    @Query(value = "update youtube_view_history set list_id=''",nativeQuery = true)
    public void reset_HistoryView();
}
