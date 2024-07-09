package com.nts.awspremium.repositories;

import com.nts.awspremium.model.InstagramCommentHistory;
import com.nts.awspremium.model.XCommentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InstagramCommentHistoryRepository extends JpaRepository<InstagramCommentHistory,String> {
    @Query(value = "SELECT list_id FROM instagram_comment_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_PostId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM instagram_comment_history where account_id=?1 limit 1",nativeQuery = true)
    public InstagramCommentHistory get_By_AccountId(String account_id);
}
