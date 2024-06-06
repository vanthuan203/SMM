package com.nts.awspremium.repositories;

import com.nts.awspremium.model.TikTokFollowerHistory;
import com.nts.awspremium.model.TikTokLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TikTokLikeHistoryRepository extends JpaRepository<TikTokLikeHistory,String> {
    @Query(value = "SELECT list_id FROM tiktok_like_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_VideoId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM tiktok_like_history where account_id=?1 limit 1",nativeQuery = true)
    public TikTokLikeHistory get_By_AccountId(String account_id);
}
