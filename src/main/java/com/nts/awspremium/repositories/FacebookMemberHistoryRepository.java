package com.nts.awspremium.repositories;

import com.nts.awspremium.model.FacebookMemberHistory;
import com.nts.awspremium.model.FacebookViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FacebookMemberHistoryRepository extends JpaRepository<FacebookMemberHistory,String> {
    @Query(value = "SELECT list_id FROM facebook_member_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_GroupId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM facebook_member_history where account_id=?1 limit 1",nativeQuery = true)
    public FacebookMemberHistory get_By_AccountId(String account_id);
}
