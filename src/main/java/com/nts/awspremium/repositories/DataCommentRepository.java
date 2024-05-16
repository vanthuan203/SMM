package com.nts.awspremium.repositories;

import com.nts.awspremium.model.DataComment;
import com.nts.awspremium.model.DataOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface DataCommentRepository extends JpaRepository<DataComment,Long> {
    @Query(value = "SELECT id,comment FROM datacomment WHERE orderid=?1 and running=1 and username=?2 limit 1",nativeQuery = true)
    public String getCommentByOrderIdAndUsername(Long orderid,String username);

    @Query(value = "SELECT count(*) FROM datacomment WHERE id=?1 and running=1 and username=?2 limit 1",nativeQuery = true)
    public Integer getCommentByCommentIdAndUsername(Long id,String username);

    @Query(value = "SELECT comment FROM datacomment WHERE id=?1 limit 1",nativeQuery = true)
    public String getCommentByCommentId(Long id);

    @Query(value = "SELECT count(*) FROM datacomment WHERE id=?1 limit 1",nativeQuery = true)
    public Integer checkByCommentId(Long id);

    @Query(value = "SELECT id FROM datacomment WHERE orderid=?1 and comment=?2 limit 1",nativeQuery = true)
    public Long getByCommentId(Long orderid,String comment);

    @Modifying
    @Transactional
    @Query(value = "update datacomment set running=1,timeget=?1,username=?2,vps=?3 where orderid  in (select orderid from videocomment) and orderid=?4 and running=0 and username='' order by rand() limit 1",nativeQuery = true)
    public void updateRunningComment(Long timeget,String username,String vps,Long orderid);
    @Modifying
    @Transactional
    @Query(value = "update datacomment set running=2 where id=?1",nativeQuery = true)
    public void updateRunningCommentDone(Long id);

    @Modifying
    @Transactional
    @Query(value = "update datacomment set running=0,username='',vps='' where vps=?1 and running=1",nativeQuery = true)
    public void resetRunningCommentByVPS(String vps);

    @Modifying
    @Transactional
    @Query(value = "update  datacomment set running=0,username='',vps='' where round((UNIX_TIMESTAMP()-timeget/1000)/60)>40 and running=1",nativeQuery = true)
    public void resetRunningCommentByCron();

    @Modifying
    @Transactional
    @Query(value = "update datacomment set running=0,username='',vps='' where running=1 and username in(select username from historycomment where running=0)",nativeQuery = true)
    public void resetRunningCommentByRunningHisCron();

    @Modifying
    @Transactional
    @Query(value = "delete FROM AccPremium.datacomment where orderid not in (select orderid from videocomment)",nativeQuery = true)
    public void deleteCommentDoneByCron();
}
