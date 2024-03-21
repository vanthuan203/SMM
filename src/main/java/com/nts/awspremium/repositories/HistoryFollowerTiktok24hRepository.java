package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryFollower24hTikTok;
import com.nts.awspremium.model.HistoryFollowerTikTok;
import com.nts.awspremium.model.HistoryTikTok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface HistoryFollowerTiktok24hRepository extends JpaRepository<HistoryFollower24hTikTok,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from history_follower_24h_tiktok where round((UNIX_TIMESTAMP()-time/1000)/60/60)>=24;",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select count(*) from history_follower_24h_tiktok where code like ?1",nativeQuery = true)
    public Integer countFollower24hByUsername(String username);
}
