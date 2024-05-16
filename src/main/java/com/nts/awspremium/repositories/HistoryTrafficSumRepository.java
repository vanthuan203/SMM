package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryTraffic;
import com.nts.awspremium.model.HistoryTraficSum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HistoryTrafficSumRepository extends JpaRepository<HistoryTraficSum,Long> {

    @Query(value = "select * from historytrafficsum where orderid=?1 order by time desc",nativeQuery = true)
    public List<HistoryTraficSum> analyticsByOrderId(Long orderid);

}
