package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ThreadsViewHistory;
import com.nts.awspremium.model.XViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThreadsViewHistoryRepository extends JpaRepository<ThreadsViewHistory,String> {
    @Query(value = "SELECT list_id FROM threads_view_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_VideoId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM threads_view_history where account_id=?1 limit 1",nativeQuery = true)
    public ThreadsViewHistory get_By_AccountId(String account_id);
}
