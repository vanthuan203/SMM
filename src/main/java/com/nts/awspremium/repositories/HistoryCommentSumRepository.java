package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryCommentSum;
import com.nts.awspremium.model.HistoryViewSum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HistoryCommentSumRepository extends JpaRepository<HistoryCommentSum,Long> {

    @Query(value = "SELECT count(*) FROM historycommentsum where commentid=?1 limit 1",nativeQuery = true)
    public Integer checkCommentIdTrue(Long commentid);

}
