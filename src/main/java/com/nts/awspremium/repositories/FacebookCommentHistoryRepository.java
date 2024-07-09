package com.nts.awspremium.repositories;

import com.nts.awspremium.model.FacebookCommentHistory;
import com.nts.awspremium.model.TikTokCommentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FacebookCommentHistoryRepository extends JpaRepository<FacebookCommentHistory,String> {
    @Query(value = "SELECT list_id FROM facebook_comment_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_PostId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM facebook_comment_history where account_id=?1 limit 1",nativeQuery = true)
    public FacebookCommentHistory get_By_AccountId(String account_id);
}
