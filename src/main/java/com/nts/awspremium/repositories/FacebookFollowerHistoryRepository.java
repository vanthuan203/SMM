package com.nts.awspremium.repositories;

import com.nts.awspremium.model.FacebookFollowerHistory;
import com.nts.awspremium.model.TikTokFollowerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FacebookFollowerHistoryRepository extends JpaRepository<FacebookFollowerHistory,String> {
    @Query(value = "SELECT list_id FROM facebook_follower_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_Id_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM facebook_follower_history where account_id=?1 limit 1",nativeQuery = true)
    public FacebookFollowerHistory get_By_AccountId(String account_id);
}
