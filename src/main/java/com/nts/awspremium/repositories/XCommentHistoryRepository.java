package com.nts.awspremium.repositories;

import com.nts.awspremium.model.FacebookCommentHistory;
import com.nts.awspremium.model.XCommentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface XCommentHistoryRepository extends JpaRepository<XCommentHistory,String> {
    @Query(value = "SELECT list_id FROM x_comment_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_PostId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM x_comment_history where account_id=?1 limit 1",nativeQuery = true)
    public XCommentHistory get_By_AccountId(String account_id);
}
