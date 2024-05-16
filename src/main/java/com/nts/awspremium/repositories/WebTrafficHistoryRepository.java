package com.nts.awspremium.repositories;

import com.nts.awspremium.model.VideoViewHistory;
import com.nts.awspremium.model.WebTraffic;
import com.nts.awspremium.model.WebTrafficHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface WebTrafficHistoryRepository extends JpaRepository<WebTrafficHistory,Long> {

    @Query(value = "SELECT * from webtraffichistory where round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=10 order by enddate desc",nativeQuery = true)
    public List<WebTrafficHistory> getWebTrafficHistories();

    @Query(value = "SELECT * from webtraffichistory where user=?1 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=10 order by enddate desc",nativeQuery = true)
    public List<WebTrafficHistory> getWebTrafficHistories(String user);

    @Query(value = "SELECT count(*) from webtraffichistory where orderid=?1 and token=?2 ",nativeQuery = true)
    public Integer checkTrueByOrderIdAndToken(Long orderid,String token);

    @Query(value = "SELECT orderid from webtraffichistory where link=?1 limit 1",nativeQuery = true)
    public Long getOrderIdWebTrafficHistoryByLink(String link);
    @Query(value = "SELECT * from webtraffichistory where orderid=?1",nativeQuery = true)
    public WebTrafficHistory getWebTrafficHisById(Long orderid);

    @Query(value = "SELECT * from webtraffichistory where orderid in (?1)",nativeQuery = true)
    public List<WebTrafficHistory> getWebTrafficHisByListId(List<String> list_orderid);
}
