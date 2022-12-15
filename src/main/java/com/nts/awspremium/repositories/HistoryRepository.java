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

    @Query(value = "SELECT * FROM history where id=?1 limit 1",nativeQuery = true)
    public List<History> getHistoriesById(Long id);
    @Query(value = "SELECT id FROM history where username=?1 limit 1",nativeQuery = true)
    public Long getId(String username);

    @Query(value = "SELECT id FROM AccPremium.history where vps like ?1 and running=0 and username not in (select username from historysum where round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by username having sum(duration)>= 43000  order by sum(duration) asc) order by timeget asc limit 1",nativeQuery = true)
    public Long getIdAccBuff(String vps);

    @Query(value = "SELECT id FROM AccPremium.history where vps like ?1 and running=0  order by timeget,rand() limit 1",nativeQuery = true)
    public Long getIdAccBuffCongchieu(String vps);

    @Query(value = "SELECT * FROM history where round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24)>=1 and username=?1 limit 1",nativeQuery = true)
    public List<History> checkEndTrial(String username);
    @Modifying
    @Transactional
    @Query(value = "UPDATE history SET running=0,vps='' where username=?1",nativeQuery = true)
    public Integer resetThreadByUsername(String username);


    @Modifying
    @Transactional
    @Query(value = "update history set running=0,videoid='' where running=1 and POSITION(videoid in listvideo)=0 and  round((UNIX_TIMESTAMP()-timeget/1000)/60)>=10",nativeQuery = true)
    public Integer resetThreadcron();
    @Modifying
    @Transactional
    @Query(value = "UPDATE history SET running=0,vps='' where vps like ?1",nativeQuery = true)
    public Integer resetThreadByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE history SET running=0,videoid='' where vps like ?1",nativeQuery = true)
    public Integer resetThreadBuffhByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE history SET running=0,vps='' where id=?1",nativeQuery = true)
    public Integer resetThreadById(Long id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE history SET running=0,videoid='' where id=?1",nativeQuery = true)
    public Integer resetThreadBuffhById(Long id);
    @Modifying
    @Transactional
    @Query(value = "UPDATE history SET vps='',running=0 where INSTR(?1,id)",nativeQuery = true)
    public Integer deletenamevpsByVps(String listId);

    @Query(value = "SELECT * FROM history where vps=?1",nativeQuery = true)
    public List<History> findHistoriesByVps(String vps);

    @Query(value = "SELECT count(*) FROM history where vps=?1 and running=1",nativeQuery = true)
    public Integer getrunningbyVps(String vps);

    @Query(value = "SELECT count(*) FROM history where username=?1 and round((UNIX_TIMESTAMP()-timeget/1000))>duration",nativeQuery = true)
    public Integer checkDurationBuffhByTimecheck(String username,Long duration);

    @Query(value = "SELECT id FROM history where vps=?1 and running=1",nativeQuery = true)
    public List<Long> getHistoryIdbyVps(String vps);

    @Query(value = "SELECT count(*) FROM history where round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 and round((UNIX_TIMESTAMP()-timeget/1000)/60/60)<12;",nativeQuery = true)
    public Integer countAccountByProxy();

    @Query(value = "SELECT vps,round((UNIX_TIMESTAMP()-max(timeget)/1000)/60) as time,count(*) as total FROM AccPremium.history where running=1 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getvpsrunning();

    @Query(value = "SELECT vps,1 as time,count(*) as total FROM AccPremium.historysum where round((UNIX_TIMESTAMP()-id/1000)/60/60) <24 group by vps",nativeQuery = true)
    public List<VpsRunning> getvpsview();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM AccPremium.historysum where round((UNIX_TIMESTAMP()-id/1000)/60/60) >24 ",nativeQuery = true)
    public Integer deleteAllViewThan24h();
}
