package com.nts.awspremium.repositories;
import com.nts.awspremium.model.HistoryView;
import com.nts.awspremium.model.VpsRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HistoryViewRepository extends JpaRepository<HistoryView,Long> {
    @Query(value = "SELECT * FROM historyview where username=?1 order by id desc limit 1",nativeQuery = true)
    public List<HistoryView> get(String username);

    @Query(value = "SELECT * FROM historyview where id=?1 limit 1",nativeQuery = true)
    public List<HistoryView> getHistoriesById(Long id);

    @Query(value = "SELECT listvideo FROM historyview where id=?1 limit 1",nativeQuery = true)
    public String getListVideoById(Long id);
    @Query(value = "SELECT id FROM historyview where username=?1 limit 1",nativeQuery = true)
    public Long getId(String username);

    @Query(value = "SELECT id FROM AccPremium.historyview where running=0 and vps=?1 order by timeget asc limit 1;",nativeQuery = true)
    public Long getAccToViewNoCheckProxy(String vps);

    @Query(value = "SELECT id FROM AccPremium.historyview where running=0 and vps=?1 order by timeget asc limit 1;",nativeQuery = true)
    public Long getAccToLive(String vps);

    @Modifying
    @Transactional
    @Query(value = "update historyview set running=0,videoid='' where running=1 and POSITION(videoid in listvideo)=0 and  round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20",nativeQuery = true)
    public Integer resetThreadcron();


    @Query(value = "select count(*) from INFORMATION_SCHEMA.PROCESSLIST where db = 'AccPremium' and COMMAND='Query' and TIME>0",nativeQuery = true)
    public Integer PROCESSLISTVIEW();
    @Modifying
    @Transactional
    @Query(value = "update historyview set running=0 where round((UNIX_TIMESTAMP()-timeget/1000)/60)>=90 and running=1",nativeQuery = true)
    public Integer resetThreadThan90mcron();

    @Modifying
    @Transactional
    @Query(value = "UPDATE historyview SET running=0,videoid='',orderid=0,geo='',typeproxy='',vps='',proxy='' where vps=?1",nativeQuery = true)
    public Integer resetHistoryViewByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE historyview SET running=0,videoid='',orderid=0 where vps=?1",nativeQuery = true)
    public Integer resetThreadViewByVps(String vps);


    @Modifying
    @Transactional
    @Query(value = "UPDATE historyview SET running=0,vps='',proxy='',typeproxy='',geo='' where id=?1",nativeQuery = true)
    public Integer resetHistoryById(Long id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE historyview SET running=0,videoid='',orderid=0 where id=?1",nativeQuery = true)
    public Integer resetThreadBuffhById(Long id);

    @Query(value = "SELECT count(*) FROM historyview where vps=?1 and running=1",nativeQuery = true)
    public Integer getrunningbyVps(String vps);

    @Query(value = "SELECT count(*) FROM historyview where id=?1 and round((UNIX_TIMESTAMP()-timeget/1000))>?2",nativeQuery = true)
    public Integer checkDurationViewByTimecheck(Long id,Long duration);


    @Query(value = "SELECT vps,round((UNIX_TIMESTAMP()-max(timeget)/1000)/60) as time,count(*) as total FROM historyview where running=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getvpsrunning();

    @Query(value = "SELECT count(*) FROM AccPremium.historyview where running=1 and orderid in(select orderid from videoview where service in(select service from service where geo='vn' and checktime=0));",nativeQuery = true)
    public Integer getThreadRunningViewVN();

    @Query(value = "SELECT count(*) FROM AccPremium.historyview where running=1 and orderid in(select orderid from videoview where service in(select service from service where geo='us' and checktime=0));",nativeQuery = true)
    public Integer getThreadRunningViewUS();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM historyviewsum where round((UNIX_TIMESTAMP()-id/1000)/60/60) >24 ",nativeQuery = true)
    public Integer deleteAllViewThan24h();



    @Modifying
    @Transactional
    @Query(value = "update historyview set listvideo=CONCAT(listvideo,\",\",?1) where id=?2",nativeQuery = true)
    public Integer updateListVideo(String videoid,Long id);

    @Modifying
    @Transactional
    @Query(value = "update historyview set listvideo=?1 where id=?2",nativeQuery = true)
    public Integer updateListVideoNew(String videoid,Long id);
}
