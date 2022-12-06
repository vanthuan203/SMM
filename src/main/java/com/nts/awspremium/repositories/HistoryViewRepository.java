package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface HistoryViewRepository extends JpaRepository<HistoryView,Long> {
    @Modifying
    @Transactional
    @Query(value = "UPDATE historyview set duration=?1  where username=?2 and videoid=?3",nativeQuery = true)
    public void updateduration(Integer duration,String username,String videoid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM historyview where username=?1 and videoid=?2",nativeQuery = true)
    public void deleteHistoryView(String username,String videoid);
}
