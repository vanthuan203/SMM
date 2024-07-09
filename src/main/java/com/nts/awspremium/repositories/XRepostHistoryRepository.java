package com.nts.awspremium.repositories;

import com.nts.awspremium.model.FacebookMemberHistory;
import com.nts.awspremium.model.XRepostHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface XRepostHistoryRepository extends JpaRepository<XRepostHistory,String> {
    @Query(value = "SELECT list_id FROM x_repost_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_PostId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM x_repost_history where account_id=?1 limit 1",nativeQuery = true)
    public XRepostHistory get_By_AccountId(String account_id);
}
