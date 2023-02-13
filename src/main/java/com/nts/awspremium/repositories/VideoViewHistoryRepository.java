package com.nts.awspremium.repositories;

import com.nts.awspremium.model.VideoView;
import com.nts.awspremium.model.VideoViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VideoViewHistoryRepository extends JpaRepository<VideoViewHistory,Long> {
    @Query(value = "SELECT * from videoviewhistory where orderid in (?1)",nativeQuery = true)
    public List<VideoViewHistory> getVideoViewHisByListId(String list_orderid);
}
