package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryFollowerTikTok;
import com.nts.awspremium.model.HistoryTikTok;
import com.nts.awspremium.model.HistoryViewYoutube;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface HistoryViewYoutubeRepository extends JpaRepository<HistoryViewYoutube,String> {
    @Query(value = "SELECT list_video FROM history_view_youtube where username=?1 limit 1",nativeQuery = true)
    public String getListVideoID(String username);

    @Query(value = "SELECT * FROM history_view_youtube where username=?1 limit 1",nativeQuery = true)
    public HistoryViewYoutube getHistoriesByUsername(String username);

}
