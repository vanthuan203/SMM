package com.nts.awspremium.repositories;

import com.nts.awspremium.model.DataComment;
import com.nts.awspremium.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import javax.transaction.Transactional;

public interface DataCommentRepository extends JpaRepository<DataComment,Long> {

    @Modifying
    @Transactional
    @Query(value = "update data_comment set running=1,get_time=?1,account_id=?2 where  order_id=?3 and running=0 and account_id='' order by rand() limit 1",nativeQuery = true)
    public void update_Running_Comment(Long get_time,String account_id,Long order_id);

    @Modifying
    @Transactional
    @QueryHints({ @QueryHint(name = "javax.persistence.query.timeout", value = "500") }) // 5 giây
    @Query(value = "UPDATE DataComment d set d.running=1,d.get_time=?1,d.account_id=?2 where  d.comment_id=?3 and d.running=0 and d.account_id=''")
    public void update_Running_Comment_By_CommentId(Long get_time,String account_id,Long comment_id);

    @Query(value = "call update_Running_Comment(?1,?2,?3);",nativeQuery = true)
    public String update_Running_Comment_PROCEDURE(Long get_time,String account_id,Long order_id);

    @Query(value = "SELECT comment FROM data_comment WHERE order_id=?1 and running=1 and account_id=?2 limit 1",nativeQuery = true)
    public String get_Comment_By_OrderId_And_Username(Long order_id,String account_id);

    @Query(value = "SELECT comment FROM data_comment WHERE comment_id=?1 and running=1 and account_id=?2 limit 1",nativeQuery = true)
    public String get_Comment_By_CommentId_And_Username(Long comment_id,String account_id);

    @Query(value = "SELECT comment_id FROM data_comment WHERE order_id=?1 and running=0 and account_id='' order by rand() limit 1",nativeQuery = true)
    public Long get_Comment_Pending_By_OrderId(Long order_id);

    @Modifying
    @Transactional
    @Query(value = "update data_comment set running=2 where  running=1 and account_id=?1 order by get_time desc limit 1",nativeQuery = true)
    public void update_Task_Comment_Done(String account_id);

    @Modifying
    @Transactional
    @Query(value = "update data_comment set running=0,account_id='' where  running=1 and account_id=?1 order by get_time desc limit 1",nativeQuery = true)
    public void update_Task_Comment_Fail(String account_id);

    @Modifying
    @Transactional
    @Query(value = "update data_comment set running=0,account_id='' where  running=1  and (account_id in(select account_id from profile_task where (running=0 or task!='comment')) or account_id not in (select account_id from profile_task))",nativeQuery = true)
    public void reset_Running_Comment();

    @Modifying
    @Transactional
    @QueryHints({ @QueryHint(name = "javax.persistence.query.timeout", value = "20000") }) // 5 giây
    @Query("UPDATE DataComment d SET d.running = 0, d.account_id = '' " +
            "WHERE d.running = 1 AND (EXISTS " +
            "(SELECT 1 FROM ProfileTask p WHERE p.account_id = d.account_id AND (p.running = 0 OR p.task <> 'comment')) " +
            "OR NOT EXISTS (SELECT 1 FROM ProfileTask p WHERE p.account_id = d.account_id))")
    void reset_Running_Comment_JPQL();

}
