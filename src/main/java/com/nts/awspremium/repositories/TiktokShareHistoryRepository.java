package com.nts.awspremium.repositories;

import com.nts.awspremium.model.TiktokShareHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TiktokShareHistoryRepository extends JpaRepository<TiktokShareHistory,String> {
    @Query(value = "SELECT list_id FROM tiktok_share_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_VideoId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM tiktok_share_history where account_id=?1 limit 1",nativeQuery = true)
    public TiktokShareHistory get_By_AccountId(String account_id);
}
