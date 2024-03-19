package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryFollowerTikTokSum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HistoryFollowerTikTokSumRepository extends JpaRepository<HistoryFollowerTikTokSum,Long> {

    @Query(value = "select * from historytrafficsum where orderid=?1 order by time desc",nativeQuery = true)
    public List<HistoryFollowerTikTokSum> analyticsByOrderId(Long orderid);

}
