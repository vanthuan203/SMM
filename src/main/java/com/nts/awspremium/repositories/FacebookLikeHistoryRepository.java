package com.nts.awspremium.repositories;

import com.nts.awspremium.model.FacebookLikeHistory;
import com.nts.awspremium.model.TikTokLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FacebookLikeHistoryRepository extends JpaRepository<FacebookLikeHistory,String> {
    @Query(value = "SELECT list_id FROM facebook_like_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_PostId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM facebook_like_history where account_id=?1 limit 1",nativeQuery = true)
    public FacebookLikeHistory get_By_AccountId(String account_id);
}
