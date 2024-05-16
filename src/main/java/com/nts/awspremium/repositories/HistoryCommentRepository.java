package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryComment;
import com.nts.awspremium.model.HistoryView;
import com.nts.awspremium.model.VpsRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HistoryCommentRepository extends JpaRepository<HistoryComment,Long> {
    @Query(value = "SELECT * FROM historycomment where username=?1 order by id desc limit 1",nativeQuery = true)
    public List<HistoryComment> get(String username);

    @Query(value = "SELECT * FROM historycomment where id=?1 limit 1",nativeQuery = true)
    public List<HistoryComment> getHistoriesById(Long id);

    @Query(value = "SELECT listvideo FROM historycomment where id=?1 limit 1",nativeQuery = true)
    public String getListVideoById(Long id);
    @Query(value = "SELECT id FROM historycomment where username=?1 limit 1",nativeQuery = true)
    public Long getId(String username);


    @Modifying
    @Transactional
    @Query(value = "update historycomment set running=0 where round((UNIX_TIMESTAMP()-timeget/1000)/60)>=40 and running=1",nativeQuery = true)
    public Integer resetThreadThan15mcron();

    @Modifying
    @Transactional
    @Query(value = "UPDATE historycomment SET running=0,videoid='',orderid=0 where vps=?1",nativeQuery = true)
    public Integer resetThreadViewByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "UPDATE historycomment SET running=0,videoid='',orderid=0 where id=?1",nativeQuery = true)
    public Integer resetThreadBuffhById(Long id);


    @Modifying
    @Transactional
    @Query(value = "update historycomment set listvideo=CONCAT(listvideo,\",\",?1) where id=?2",nativeQuery = true)
    public Integer updateListVideo(String videoid,Long id);

    @Modifying
    @Transactional
    @Query(value = "update historycomment set listvideo=?1 where id=?2",nativeQuery = true)
    public Integer updateListVideoNew(String videoid,Long id);
}
