package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryFollowerTikTokSum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HistoryFollowerTikTokSumRepository extends JpaRepository<HistoryFollowerTikTokSum,Long> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM history_follower_tiktok_sum where tiktok_id not in (select tiktok_id from channel_tiktok) limit 500000",nativeQuery = true)
    public void DelHistorySum();

}
