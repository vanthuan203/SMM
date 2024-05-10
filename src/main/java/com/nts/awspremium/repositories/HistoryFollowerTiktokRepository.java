package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryFollowerTikTok;
import com.nts.awspremium.model.HistoryTikTok;
import com.nts.awspremium.model.HistoryTraffic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HistoryFollowerTiktokRepository extends JpaRepository<HistoryFollowerTikTok,String> {
    @Query(value = "SELECT list_tiktok_id FROM history_follower_tiktok where username=?1 limit 1",nativeQuery = true)
    public String getListTiktokID(String username);

    @Query(value = "SELECT * FROM history_follower_tiktok where username=?1 limit 1",nativeQuery = true)
    public HistoryFollowerTikTok getHistoriesByUsername(String username);

}
