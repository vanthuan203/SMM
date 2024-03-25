package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryTikTok;
import com.nts.awspremium.model.HistoryTraffic;
import com.nts.awspremium.model.VpsRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HistoryTiktokRepository extends JpaRepository<HistoryTikTok,String> {
    @Query(value = "SELECT count(*) FROM history_tiktok where username=?1",nativeQuery = true)
    public Integer checkUsername(String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE history_tiktok SET running=0,orderid=0 where username=?1",nativeQuery = true)
    public Integer resetThreadByUsername(String username);
    @Query(value = "SELECT device_id as vps,round((UNIX_TIMESTAMP()-max(timeget)/1000)/60) as time,count(*) as total FROM history_tiktok where running=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getvpsrunning();
    @Query(value = "SELECT * FROM history_tiktok where id=?1 limit 1",nativeQuery = true)
    public List<HistoryTraffic> getHistoriesById(Long id);

    @Query(value = "SELECT listorderid FROM history_tiktok where id=?1 limit 1",nativeQuery = true)
    public String getListOrderIdById(Long id);
    @Query(value = "SELECT * FROM history_tiktok where username=?1 limit 1",nativeQuery = true)
    public HistoryTikTok getHistoryTikTokByUsername(String username);

    @Modifying
    @Transactional
    @Query(value = "update historytraffic set running=0,orderid=0 where running=1 and POSITION(orderid in listorderid)=0 and  round((UNIX_TIMESTAMP()-timeget/1000)/60)>=15",nativeQuery = true)
    public Integer resetThreadcron();



    @Query(value = "select count(*) from INFORMATION_SCHEMA.PROCESSLIST where db = 'AccPremium' and COMMAND='Query' and TIME>0",nativeQuery = true)
    public Integer PROCESSLISTVIEW();
    @Modifying
    @Transactional
    @Query(value = "update historytraffic set running=0,orderid=0 where round((UNIX_TIMESTAMP()-timeget/1000)/60)>=30 and running=1",nativeQuery = true)
    public Integer resetThreadThan30mcron();


    @Modifying
    @Transactional
    @Query(value = "UPDATE historytraffic SET running=0,orderid=0 where vps=?1",nativeQuery = true)
    public Integer resetThreadByVps(String vps);





    @Modifying
    @Transactional
    @Query(value = "update historytraffic set listorderid=CONCAT(listorderid,\",\",?1) where id=?2",nativeQuery = true)
    public Integer updateListOrderid(String videoid,Long id);

    @Modifying
    @Transactional
    @Query(value = "update historytraffic set listorderid=?1 where id=?2",nativeQuery = true)
    public Integer updateListOrderidNew(String videoid,Long id);
}
