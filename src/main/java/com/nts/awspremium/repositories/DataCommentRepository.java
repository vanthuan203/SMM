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

    @Modifying
    @Transactional
    @Query(value = "update datacomment set running=1,timeget=?1,username=?2 where orderid  in (select orderid from videocomment) and orderid=?3 and running=0 and username='' limit 1",nativeQuery = true)
    public void updateRunningComment(Long timeget,String username,Long orderid);
    @Modifying
    @Transactional
    @Query(value = "update datacomment set running=2 where id=?1",nativeQuery = true)
    public void updateRunningCommentDone(Long id);

}
