package com.nts.awspremium.repositories;

import com.nts.awspremium.model.YoutubeCommentHistory;
import com.nts.awspremium.model.YoutubeLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface YoutubeCommentHistoryRepository extends JpaRepository<YoutubeCommentHistory,Long> {
    @Query(value = "SELECT list_id FROM youtube_comment_history where account_id=?1 limit 1",nativeQuery = true)
    public String get_List_VideoId_By_AccountId(String account_id);

    @Query(value = "SELECT * FROM youtube_comment_history where account_id=?1 limit 1",nativeQuery = true)
    public YoutubeCommentHistory get_By_AccountId(String account_id);
}
