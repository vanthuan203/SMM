package com.nts.awspremium.repositories;

import com.nts.awspremium.model.TikTokLikeHistory;
import com.nts.awspremium.model.TikTokViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TikTokViewHistoryRepository extends JpaRepository<TikTokViewHistory,String> {
    @Query(value = "SELECT list_id FROM tiktok_view_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_VideoId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM tiktok_view_history where account_id=?1 limit 1",nativeQuery = true)
    public TikTokViewHistory get_By_AccountId(String account_id);
}
