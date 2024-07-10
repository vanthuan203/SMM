package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ThreadsLikeHistory;
import com.nts.awspremium.model.XLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThreadsLikeHistoryRepository extends JpaRepository<ThreadsLikeHistory,String> {
    @Query(value = "SELECT list_id FROM threads_like_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_PostId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM threads_like_history where account_id=?1 limit 1",nativeQuery = true)
    public ThreadsLikeHistory get_By_AccountId(String account_id);
}
