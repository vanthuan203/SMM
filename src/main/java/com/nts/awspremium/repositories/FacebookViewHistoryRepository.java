package com.nts.awspremium.repositories;

import com.nts.awspremium.model.FacebookViewHistory;
import com.nts.awspremium.model.TikTokLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FacebookViewHistoryRepository extends JpaRepository<FacebookViewHistory,String> {
    @Query(value = "SELECT list_id FROM facebook_view_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_VideoId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM facebook_view_history where account_id=?1 limit 1",nativeQuery = true)
    public FacebookViewHistory get_By_AccountId(String account_id);
}
