package com.nts.awspremium.repositories;

import com.nts.awspremium.model.TikTokFollowerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TikTokAccountHistoryRepository extends JpaRepository<TikTokFollowerHistory,String> {
    @Query(value = "SELECT list_id FROM tiktok_follower_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_TiktokId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM tiktok_follower_history where account_id=?1 limit 1",nativeQuery = true)
    public TikTokFollowerHistory get_By_AccountId(String account_id);
}
