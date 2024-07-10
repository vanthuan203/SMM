package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ThreadsCommentHistory;
import com.nts.awspremium.model.XCommentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThreadsCommentHistoryRepository extends JpaRepository<ThreadsCommentHistory,String> {
    @Query(value = "SELECT list_id FROM threads_comment_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_PostId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM threads_comment_history where account_id=?1 limit 1",nativeQuery = true)
    public ThreadsCommentHistory get_By_AccountId(String account_id);
}
