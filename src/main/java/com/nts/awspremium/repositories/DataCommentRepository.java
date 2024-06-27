package com.nts.awspremium.repositories;

import com.nts.awspremium.model.DataComment;
import com.nts.awspremium.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface DataCommentRepository extends JpaRepository<DataComment,Long> {

    @Modifying
    @Transactional
    @Query(value = "update data_comment set running=1,get_time=?1,account_id=?2 where  order_id=?3 and running=0 and account_id='' order by rand() limit 1",nativeQuery = true)
    public void update_Running_Comment(Long get_time,String account_id,Long order_id);

    @Query(value = "SELECT comment FROM data_comment WHERE order_id=?1 and running=1 and account_id=?2 limit 1",nativeQuery = true)
    public String get_Comment_By_OrderId_And_Username(Long order_id,String account_id);

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
    @Query(value = "update data_comment set running=0,account_id='' where  running=1  and account_id in(select account_id from account_task where (running=0 or task!='comment')) ",nativeQuery = true)
    public void reset_Running_Comment();

}
