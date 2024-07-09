package com.nts.awspremium.repositories;

import com.nts.awspremium.model.InstagramFollowerHistory;
import com.nts.awspremium.model.XFollowerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InstagramFollowerHistoryRepository extends JpaRepository<InstagramFollowerHistory,String> {
    @Query(value = "SELECT list_id FROM instagram_follower_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_Id_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM instagram_follower_history where account_id=?1 limit 1",nativeQuery = true)
    public InstagramFollowerHistory get_By_AccountId(String account_id);
}
