package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistorySum;
import com.nts.awspremium.model.HistoryViewSum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HistoryViewSumRepository extends JpaRepository<HistoryViewSum,Long> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM hisoryview where username=?1 and videoid=?2",nativeQuery = true)
    public void DelHistoryError(String username,String videoid);
    @Query(value = "SELECT FROM_UNIXTIME(historyviewsum.time/1000,'%Y-%m-%d') as date,ROUND(sum(duration)/3600,0) as total,count(*) as view FROM historyviewsum group by date order by date desc limit 7",nativeQuery = true)
    public List<String> Gettimebuff7day();

    @Query(value = "SELECT FROM_UNIXTIME(historyviewsum.time/1000,'%Y-%m-%d') as date,ROUND(sum(duration)/3600,0) as total,count(*) as view FROM historyviewsum where videoid in (select videoview.videoid from videobuffh where videoview.user=?1) group by date order by date desc limit 7",nativeQuery = true)
    public List<String> Gettimebuff7day(String user);
}
