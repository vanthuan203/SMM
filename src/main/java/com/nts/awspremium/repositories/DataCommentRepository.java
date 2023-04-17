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

    @Query(value = "SELECT count(*) FROM datacomment WHERE orderid=?1 and running=1",nativeQuery = true)
    public Integer checkCommentDone(Long orderid);


    @Query(value = "SELECT * FROM datacomment WHERE orderid=?1 and running=1",nativeQuery = true)
    public Integer checkCommentDoneLK(Long orderid);

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
    @Query(value = "update datacomment set running=0,username='',vps='' where username=?1 and running=1",nativeQuery = true)
    public void resetRunningComment(String username);

    @Modifying
    @Transactional
    @Query(value = "update  datacomment set running=0,username='',vps='' where round((UNIX_TIMESTAMP()-timeget/1000)/60)>7 and running=1",nativeQuery = true)
    public void resetRunningCommentByCron();
}
