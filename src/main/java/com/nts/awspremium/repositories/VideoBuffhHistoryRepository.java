package com.nts.awspremium.repositories;

import com.nts.awspremium.model.VideoBuffh;
import com.nts.awspremium.model.VideoBuffhHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoBuffhHistoryRepository extends JpaRepository<VideoBuffhHistory,Long> {

    @Query(value = "SELECT * from videobuffhhistory",nativeQuery = true)
    public List<VideoBuffhHistory> getVideoBuffhHistories();

}
