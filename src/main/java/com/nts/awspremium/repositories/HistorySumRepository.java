package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistorySum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface HistorySumRepository extends JpaRepository<HistorySum,Long> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM hisory where username=?1 and videoid=?2",nativeQuery = true)
    public void DelHistoryError(String username,String videoid);
}
