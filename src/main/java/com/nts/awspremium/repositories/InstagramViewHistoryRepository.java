package com.nts.awspremium.repositories;

import com.nts.awspremium.model.InstagramViewHistory;
import com.nts.awspremium.model.XViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InstagramViewHistoryRepository extends JpaRepository<InstagramViewHistory,String> {
    @Query(value = "SELECT list_id FROM instagram_view_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_VideoId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM instagram_view_history where account_id=?1 limit 1",nativeQuery = true)
    public InstagramViewHistory get_By_AccountId(String account_id);
}
