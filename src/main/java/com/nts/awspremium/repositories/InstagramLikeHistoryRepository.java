package com.nts.awspremium.repositories;

import com.nts.awspremium.model.InstagramLikeHistory;
import com.nts.awspremium.model.XLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InstagramLikeHistoryRepository extends JpaRepository<InstagramLikeHistory,String> {
    @Query(value = "SELECT list_id FROM instagram_like_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_PostId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM instagram_like_history where account_id=?1 limit 1",nativeQuery = true)
    public InstagramLikeHistory get_By_AccountId(String account_id);
}
