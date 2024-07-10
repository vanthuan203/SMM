package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ThreadsRepostHistory;
import com.nts.awspremium.model.XRepostHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThreadsRepostHistoryRepository extends JpaRepository<ThreadsRepostHistory,String> {
    @Query(value = "SELECT list_id FROM threads_repost_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_PostId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM threads_repost_history where account_id=?1 limit 1",nativeQuery = true)
    public ThreadsRepostHistory get_By_AccountId(String account_id);
}
