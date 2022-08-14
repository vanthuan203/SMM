package com.nts.awspremium.repositories;

import com.nts.awspremium.model.History;
import com.nts.awspremium.model.Video;
import com.nts.awspremium.model.VpsRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

public interface HistoryRepository extends JpaRepository<History,Long> {
    @Query(value = "SELECT * FROM history where username=?1 order by id desc limit 1",nativeQuery = true)
    public List<History> get(String username);
    @Query(value = "SELECT * FROM history where round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24)>=1 and username=?1 limit 1",nativeQuery = true)
    public List<History> checkEndTrial(String username);
    @Modifying
    @Transactional
    @Query(value = "UPDATE history SET running=0,vps='' where username=?1",nativeQuery = true)
    public Integer resetThreadByUsername(String username);
    @Modifying
    @Transactional
    @Query(value = "UPDATE history SET vps='',running=0 where vps=?1",nativeQuery = true)
    public Integer deletenamevpsByVps(String vps);

    @Query(value = "SELECT * FROM history where vps=?1",nativeQuery = true)
    public List<History> findHistoriesByVps(String vps);

    @Query(value = "SELECT count(*) FROM history where vps=?1 and running=1",nativeQuery = true)
    public Integer getrunningbyVps(String vps);

    @Query(value = "SELECT count(*) FROM history where typeproxy=?1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 and round((UNIX_TIMESTAMP()-timeget/1000)/60/60)<12;",nativeQuery = true)
    public Integer countAccountByTypeProxy(String typeproxy);

    @Query(value = "SELECT vps,round((UNIX_TIMESTAMP()-max(timeget)/1000)/60) as time,count(*) as total FROM AccPremium.history where running=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getvpsrunning();

    @Query(value = "SELECT vps,1 as time,count(*) as total FROM AccPremium.historysum where round((UNIX_TIMESTAMP()-id/1000)/60/60) <24 group by vps",nativeQuery = true)
    public List<VpsRunning> getvpsview();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM AccPremium.historysum where round((UNIX_TIMESTAMP()-id/1000)/60/60) >24 ",nativeQuery = true)
    public Integer deleteAllViewThan24h();
}
