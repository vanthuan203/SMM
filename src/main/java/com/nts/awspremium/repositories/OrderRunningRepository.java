package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderRunning;
import com.nts.awspremium.model.OrderRunningShow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface OrderRunningRepository extends JpaRepository<OrderRunning,Long> {
    @Query(value = "SELECT * FROM order_running where start_time>0 and service_id in(select service_id from service where platform=?1 and task=?2 and mode=?3) and INSTR(?4,CONCAT(order_key,'|'))=0 and order_id in (?5) order by rand() limit 1",nativeQuery = true)
    public OrderRunning get_Order_Running_By_Task_OFF(String platform,String task,String mode,String list_tiktok_id, List<String> order_id);
    @Query(value = "SELECT o.* FROM order_running o JOIN service s ON s.service_id=o.service_id WHERE o.start_time>0 AND s.platform=?1 AND s.task=?2 AND s.mode=?3 AND INSTR(?4,CONCAT(o.order_key,'|'))=0 AND o.order_id IN (?5) ORDER BY RAND() LIMIT 1", nativeQuery = true)
    public OrderRunning get_Order_Running_By_Task(String platform,String task,String mode,String list_tiktok_id, List<String> order_id);

    @Query(value = "SELECT * FROM order_running where start_time>0 and service_id in(select service_id from service where platform=?1 and task=?2) and INSTR(?3,CONCAT(order_key,'|'))=0 and order_id in (?4) order by rand() limit 1",nativeQuery = true)
    public OrderRunning get_Order_Running_By_Task_No_Mode_OFF(String platform,String task,String list_tiktok_id, List<String> order_id);

    @Query(value = "SELECT o.* FROM order_running o JOIN service s ON s.service_id=o.service_id WHERE o.start_time>0 AND s.platform=?1 AND s.task=?2 AND INSTR(?3,CONCAT(o.order_key,'|'))=0 AND o.order_id IN (?4) ORDER BY RAND() LIMIT 1", nativeQuery = true)
    public OrderRunning get_Order_Running_By_Task_No_Mode(String platform,String task,String list_tiktok_id, List<String> order_id);


    @Query(value = "SELECT * FROM order_running where start_time>0 and service_id in(select service_id from service where platform=?1 and task=?2 and mode=?3) and INSTR(?4,CONCAT(order_key,'|'))=0 and order_id in (?5) and update_time <= (UNIX_TIMESTAMP() - 5*60) * 1000 order by rand() limit 1",nativeQuery = true)
    public OrderRunning get_Order_Running_By_Task_And_Limit_Time_OFF(String platform,String task,String mode,String list_tiktok_id, List<String> order_id);

    @Query(value = "SELECT o.* FROM order_running o JOIN service s ON s.service_id=o.service_id WHERE o.start_time>0 AND s.platform=?1 AND s.task=?2 AND s.mode=?3 AND INSTR(?4,CONCAT(o.order_key,'|'))=0 AND o.order_id IN (?5) AND o.update_time <= (UNIX_TIMESTAMP()-s.limit_task_time*60)*1000 ORDER BY RAND() LIMIT 1", nativeQuery = true)
    public OrderRunning get_Order_Running_By_Task_And_Limit_Time(String platform,String task,String mode,String list_tiktok_id, List<String> order_id);

    @Query(value = "SELECT o.* FROM order_running o JOIN service s ON s.service_id=o.service_id WHERE o.start_time>0 AND s.platform=?1 AND s.task=?2 AND s.mode=?3 AND INSTR(?4,CONCAT(o.order_key,'|'))=0 AND o.order_id IN (?5) AND o.update_time <= (UNIX_TIMESTAMP()-s.limit_task_time*60)*1000 AND o.priority>0 ORDER BY RAND() LIMIT 1", nativeQuery = true)
    public OrderRunning get_Order_Running_By_Task_Priority_And_Limit_Time(String platform,String task,String mode,String list_tiktok_id, List<String> order_id);


    @Query(value = "SELECT * FROM order_running where start_time>0 and service_id in(select service_id from service where platform=?1 and task=?2 and mode=?3) and INSTR(?4,CONCAT(order_key,'|'))=0 and order_id in (?5) and priority>0 order by rand() limit 1",nativeQuery = true)
    public OrderRunning get_Order_Running_Priority_By_Task_OFF(String platform,String task,String mode,String list_tiktok_id, List<String> order_id);

    @Query(value = "SELECT o.* FROM order_running o JOIN service s ON s.service_id=o.service_id WHERE o.start_time>0 AND s.platform=?1 AND s.task=?2 AND s.mode=?3 AND INSTR(?4,CONCAT(o.order_key,'|'))=0 AND o.order_id IN (?5) AND o.priority>0 ORDER BY RAND() LIMIT 1", nativeQuery = true)
    public OrderRunning get_Order_Running_Priority_By_Task(String platform,String task,String mode,String list_tiktok_id, List<String> order_id);



    @Query(value = "SELECT * FROM order_running where start_time>0 and service_id in(select service_id from service where platform=?1 and task=?2) and INSTR(?3,CONCAT(order_key,'|'))=0 and order_id in (?4) and priority>0 order by rand() limit 1",nativeQuery = true)
    public OrderRunning get_Order_Running_Priority_By_Task_No_Mode_OFF(String platform,String task,String list_tiktok_id, List<String> order_id);

    @Query(value = "SELECT o.* FROM order_running o JOIN service s ON s.service_id=o.service_id WHERE o.start_time>0 AND s.platform=?1 AND s.task=?2 AND INSTR(?3,CONCAT(o.order_key,'|'))=0 AND o.order_id IN (?4) AND o.priority>0 ORDER BY RAND() LIMIT 1", nativeQuery = true)
    public OrderRunning get_Order_Running_Priority_By_Task_No_Mode(String platform,String task,String list_tiktok_id, List<String> order_id);


    @Query(value = "select order_id from (select order_running.order_id,count(running) as total,thread\n" +
            "                      from order_running left join profile_task on profile_task.order_id=order_running.order_id and running=1\n" +
            "                       group by order_id having total<thread) as t",nativeQuery = true)
    public List<String> get_List_Order_Thread_True();

    @Query(value = "select order_id from (select order_running.order_id,count(running) as total,thread\n" +
            "                              from order_running left join profile_task on profile_task.order_id=order_running.order_id and running=1 and profile_task.task=?1  where service_id in (select service_id from service where task=?1)\n" +
            "                                 group by order_id having total<thread) as t",nativeQuery = true)
    public List<String> get_List_Order_Thread_By_Task_True(String task);

    @Query(value = "select order_id from (select order_running.order_id,count(running) as total,thread\n" +
            "                      from order_running left join profile_task on profile_task.order_id=order_running.order_id and running=1\n" +
            "                       group by order_id having total<4*thread) as t",nativeQuery = true)
    public List<String> get_List_Order_Thread_SpeedUp_True();

    @Query(value = "SELECT o from OrderRunning o JOIN FETCH o.service where o.order_id=?1")
    public OrderRunning get_Order_By_Id(Long order_id);

    @Query(value = "SELECT count(*) from order_running where order_key=?1",nativeQuery = true)
    public Integer get_Order_By_Order_Key(String order_id);

    @Query(value = "SELECT count(*) from order_running where check_count=1 and order_id=?1",nativeQuery = true)
    public Integer check_Check_Count(Long order_id);

    @Query(value = "SELECT o from OrderRunning o JOIN FETCH o.service where o.service.check_count=1 and o.total>0 and o.start_time>0 and (o.update_current_time<o.update_time or (?1-o.update_current_time)/1000/60>=30)")
    public List<OrderRunning> get_Order_By_Check_Count(Long now);

    @Query(value = "SELECT o from OrderRunning o JOIN FETCH o.service WHERE o.check_count=?1 ORDER BY o.update_current_time ASC")
    public List<OrderRunning> get_Order_By_Check_Count_Num(Integer check_count);
    @Query(value = "SELECT count(*) from order_running where order_key=?1 and service_id in(select service_id from service where task=?2)",nativeQuery = true)
    public Integer get_Order_By_Order_Key_And_Task(String order_id,String task);

    @Query(value = "SELECT count(*) from order_running where channel_id=?1",nativeQuery = true)
    public Integer get_Order_By_ChannelId(String channel_id);

    @Query(value = "SELECT count(*) from order_running where order_key=?1 and username='refill@gmail.com' and service_id in(select service_id from service where task=?2)",nativeQuery = true)
    public Integer get_Order_Refill_By_Order_Key_And_Task(String order_id,String task);

    @Query(value = "SELECT * from order_running where order_key=?1 and  service_id in(select service_id from service where task=?2 and platform=?3) limit 1",nativeQuery = true)
    public OrderRunning find_Order_By_Order_Key(String order_key,String task,String platform);

    @Query(value = "SELECT * from order_running where order_id=?1 limit 1",nativeQuery = true)
    public OrderRunning find_Order_By_OrderId(Long order_id);

    @Query(value = "SELECT o.*,s.task,s.platform from order_running o left join service s on o.service_id=s.service_id where o.start_count_time=0 and s.platform=?1 and o.check_count=0 order by rand() limit 1",nativeQuery = true)
    public OrderRunningShow find_Order_By_Start_Count0(String platform);

    @Query(value = "SELECT o.*,s.task,s.platform from order_running o left join service s on o.service_id=s.service_id where o.total>0 and round((UNIX_TIMESTAMP()-o.update_current_time/1000)/60)>=3 and s.platform=?1 and o.check_count=0 order by rand() limit 1",nativeQuery = true)
    public OrderRunningShow find_Order_By_Curent0(String platform);

    @Query(value = "SELECT * from order_running where order_id in (?1)",nativeQuery = true)
    public List<OrderRunning> get_Order_By_ListId(List<String> list_orderid);

    ///@Query(value = "select o from OrderRunning o JOIN FETCH o.service where round((UNIX_TIMESTAMP()-o.update_time/1000)/60)>=30")//
    //public List<OrderRunning> get_Order_Check_Valid();

    @Query("select o from OrderRunning o JOIN FETCH o.service where o.start_time>0 and (:currentTime - o.start_time) >= :threshold and ((:currentTime - o.update_time) >= :threshold or o.valid=0)")
    public List<OrderRunning> get_Order_Check_Valid(@Param("currentTime") long currentTime, @Param("threshold") long threshold);

    @Query(value = "SELECT COALESCE(SUM(thread_set), 0) AS total_threads\n" +
            "FROM Data.order_running\n" +
            "WHERE start_time > 0 \n" +
            "AND service_id IN (SELECT service_id FROM service WHERE mode = 'auto' and task='follower');",nativeQuery = true)
    public Integer get_Sum_Thread_Running_By_Mode_Auto();

    @Query(value = "SELECT COALESCE(SUM(thread_set), 0) AS total_threads\n" +
            "FROM Data.order_running\n" +
            "WHERE start_time = 0 \n" +
            "AND service_id IN (SELECT service_id FROM service WHERE mode = 'auto' and task='follower');",nativeQuery = true)
    public Integer get_Sum_Thread_Pending_By_Mode_Auto();

    @Query(value = "SELECT COALESCE(SUM(thread_set), 0) FROM Data.order_running where start_time>0 and service_id in(SELECT service_id FROM service where mode='auto' and task='view');",nativeQuery = true)
    public Integer get_Sum_Thread_Order_Running_By_Mode_Auto();

    @Query(value = "SELECT count(*) FROM Data.order_running where service_id=?1 and start_time>0;",nativeQuery = true)
    public Integer get_Count_OrderRunning_By_Service_And_StartTime(Integer service_id);

    @Query(value = "SELECT count(*) FROM Data.order_running where service_id=?1",nativeQuery = true)
    public Integer get_Count_OrderRunning_By_Service(Integer service_id);

    @Query(value = "SELECT count(*) FROM Data.order_running where username=?1",nativeQuery = true)
    public Integer get_Count_OrderRunning_By_User(String username);



    @Query(value = "select o from OrderRunning o JOIN FETCH o.service where o.start_time=0 ORDER BY o.service.task ASC,o.priority DESC,o.insert_time ASC")
    public List<OrderRunning> get_Order_Pending_ASC();

    @Query(value = "select o from OrderRunning o JOIN FETCH o.service  where o.start_time>0 and o.service.task='comment' and o.service.ai=true and o.order_id in (select c.orderRunning.order_id from OrderComment c where c.count_render<o.quantity*2) ORDER BY o.service.task ASC,o.priority DESC,o.insert_time ASC")
    public List<OrderRunning> get_Order_RenderCommentAI_ASC();

    @Query(value = "SELECT service_id from order_running where order_key=?1 and service_id in(select service_id from service where task='view' and platform='youtube')",nativeQuery = true)
    public Integer get_ServiceId_By_TaskKey(String order_key);

    @Query(value = "SELECT count(*) from order_running where order_key=?1 and service_id in(select service_id from service where history=0)",nativeQuery = true)
    public Integer check_No_History(String order_key);
    @Query(value = "SELECT order_running.order_id,count(*) as total FROM history_sum join order_running  on history_sum.order_id=order_running.order_id where order_running.start_time>0 group by order_running.order_id order by start_time desc",nativeQuery = true)
    public List<String> get_Total_Buff_Cron();

    @Query(value = "SELECT order_running.order_id,count(*) as total FROM history_sum join order_running  on history_sum.order_id=order_running.order_id where order_running.start_time>0 and history_sum.add_time >= (UNIX_TIMESTAMP() - ?1*60*60) * 1000 group by order_running.order_id order by start_time desc",nativeQuery = true)
    public List<String> get_Total_Buff_By_AddTime_Cron(Integer hour);

    @Query(value = "select o from OrderRunning o JOIN FETCH o.service  where o.service.check_done=?1 and o.service.bonus_check=false and o.total>=(o.quantity+o.quantity*(o.service.bonus/100)) order by o.start_time asc")
    public List<OrderRunning> get_Order_Running_Done(Integer check_done);

    @Query(value = "select o from OrderRunning o JOIN FETCH o.service  where o.service.check_done=1 and o.service.bonus_check=true and o.total>=(o.quantity+o.total_limit_time*(o.service.bonus/100)) order by o.start_time asc")
    public List<OrderRunning> get_Order_Running_Done_Bonus_Check();

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set update_time=IF(total<?1,?2,update_time),total=?1 where order_id=?3",nativeQuery = true)
    public void update_Total_Buff_By_OrderId(Integer total,Long update_time,Long order_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set total_limit_time=?1 where order_id=?2",nativeQuery = true)
    public void update_Total_Buff_Limit_Time_By_OrderId(Integer total_limit_time,Long order_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set current_count=?1,update_current_time=?2,check_count_time=total where order_id=?3",nativeQuery = true)
    public void update_Current_Count(Integer current_count,Long update_current_time,Long order_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set valid=?1 where order_id=?2",nativeQuery = true)
    public void update_Valid_By_OrderId(Integer valid,Long order_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set valid=?1,channel_title=?2 where order_id=?3",nativeQuery = true)
    public void update_ChannelTitle_And_Valid_By_OrderId(Integer valid,String channel_title,Long order_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set valid=?1,channel_title=?2,order_key=?3,order_link=?4 where order_id=?5",nativeQuery = true)
    public void update_OrderInfo_ChannelTitle_And_Valid_By_OrderId(Integer valid,String channel_title,String order_key,String order_link,Long order_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set check_count=1,check_count_time=?1 where order_id=?2",nativeQuery = true)
    public void update_Check_Count(Long check_count_time,Long order_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set check_count=0 where round((UNIX_TIMESTAMP()-check_count_time/1000))>=30",nativeQuery = true)
    public void reset_Check_Count();

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set check_count=0;",nativeQuery = true)
    public void reset_Check_Count_ALL();

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set check_count=?1 where order_id=?2 ",nativeQuery = true)
    public void update_Check_Count_By_OrderId(Integer check_count,Long order_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM order_running where order_id=?1",nativeQuery = true)
    public void delete_Order_Running_By_OrderId(Long order_id);

    @Query(value = "Select o.order_id,o.order_key,o.order_link,count(running) as total_thread\n" +
            ",o.thread,o.priority,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.total_limit_time,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.charge,o.service_id,s.platform,s.check_count,o.check_count_time,s.bonus,\n" +
            "s.task,s.mode,s.bonus_check from order_running o \n" +
            "left join profile_task a on a.order_id=o.order_id and running=1 \n" +
            "left join service s on o.service_id=s.service_id where o.username!='refill@gmail.com' and  o.start_time>0 \n" +
            "group by o.order_id order by o.start_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Running();

    @Query(
            value =
                    "SELECT " +
                            " o.order_id,o.order_key,o.order_link, " +
                            " COUNT(a.running) AS total_thread, " +
                            " o.thread,o.priority,o.insert_time,o.start_time,o.note, " +
                            " o.start_count,o.quantity,o.username,o.total,o.total_limit_time,o.current_count, " +
                            " o.update_time,o.update_current_time,o.charge,o.service_id, " +
                            " s.platform,s.check_count,o.check_count_time,s.bonus, " +
                            " s.task,s.mode,s.bonus_check " +
                            "FROM order_running o " +
                            "LEFT JOIN profile_task a " +
                            " ON a.order_id = o.order_id AND a.running = 1 " +
                            "LEFT JOIN service s " +
                            " ON o.service_id = s.service_id " +
                            "WHERE o.username <> 'refill@gmail.com' " +
                            " AND o.start_time > 0 " +
                            "GROUP BY o.order_id",
            countQuery =
                    "SELECT COUNT(DISTINCT o.order_id) " +
                            "FROM order_running o " +
                            "LEFT JOIN profile_task a " +
                            " ON a.order_id = o.order_id AND a.running = 1 " +
                            "LEFT JOIN service s " +
                            " ON o.service_id = s.service_id " +
                            "WHERE o.username <> 'refill@gmail.com' " +
                            " AND o.start_time > 0",
            nativeQuery = true
    )
    Page<OrderRunningShow> getOrderRunning(Pageable pageable);


    @Query(
            value =
                    "SELECT " +
                            " o.order_id,o.order_key,o.order_link, " +
                            " COUNT(a.running) AS total_thread, " +
                            " o.thread,o.priority,o.insert_time,o.start_time,o.note, " +
                            " o.start_count,o.quantity,o.username,o.total,o.total_limit_time,o.current_count, " +
                            " o.update_time,o.update_current_time,o.charge,o.service_id, " +
                            " s.platform,s.check_count,o.check_count_time,s.bonus, " +
                            " s.task,s.mode,s.bonus_check " +
                            "FROM order_running o " +
                            "LEFT JOIN profile_task a " +
                            " ON a.order_id = o.order_id AND a.running = 1 " +
                            "LEFT JOIN service s " +
                            " ON o.service_id = s.service_id " +
                            "WHERE o.username <> 'refill@gmail.com' " +
                            " AND o.start_time > 0 " +
                            " AND (o.order_id IN (:orderIds) OR o.order_key IN (:orderIds)) " +
                            "GROUP BY o.order_id",
            countQuery =
                    "SELECT COUNT(DISTINCT o.order_id) " +
                            "FROM order_running o " +
                            "WHERE o.username <> 'refill@gmail.com' " +
                            " AND o.start_time > 0 " +
                            " AND (o.order_id IN (:orderIds) OR o.order_key IN (:orderIds))",
            nativeQuery = true
    )
    Page<OrderRunningShow> getOrderRunning(
            @Param("orderIds") List<String> orderIds,
            Pageable pageable
    );


    @Query(
            value =
                    "SELECT " +
                            " o.order_id,o.order_key,o.order_link, " +
                            " COUNT(a.running) AS total_thread, " +
                            " o.thread,o.priority,o.insert_time,o.start_time,o.note, " +
                            " o.start_count,o.quantity,o.username,o.total,o.total_limit_time,o.current_count, " +
                            " o.update_time,o.update_current_time,o.charge,o.service_id, " +
                            " s.platform,s.check_count,o.check_count_time,s.bonus, " +
                            " s.task,s.mode,s.bonus_check " +
                            "FROM order_running o " +
                            "LEFT JOIN profile_task a " +
                            " ON a.order_id = o.order_id AND a.running = 1 " +
                            "LEFT JOIN service s " +
                            " ON o.service_id = s.service_id " +
                            "WHERE o.username <> 'refill@gmail.com' " +
                            " AND o.username = (:user) " +
                            " AND o.start_time > 0 " +
                            " AND (o.order_id IN (:orderIds) OR o.order_key IN (:orderIds)) " +
                            "GROUP BY o.order_id",
            countQuery =
                    "SELECT COUNT(DISTINCT o.order_id) " +
                            "FROM order_running o " +
                            "WHERE o.username <> 'refill@gmail.com' " +
                            " AND o.username = (:user) " +
                            " AND o.start_time > 0 " +
                            " AND (o.order_id IN (:orderIds) OR o.order_key IN (:orderIds))",
            nativeQuery = true
    )
    Page<OrderRunningShow> getOrderRunningUser(
            @Param("user") String user,
            @Param("orderIds") List<String> orderIds,
            Pageable pageable
    );



    @Query(
            value =
                    "SELECT " +
                            " o.order_id,o.order_key,o.order_link, " +
                            " COUNT(a.running) AS total_thread, " +
                            " o.thread,o.priority,o.insert_time,o.start_time,o.note, " +
                            " o.start_count,o.quantity,o.username,o.total,o.total_limit_time,o.current_count, " +
                            " o.update_time,o.update_current_time,o.charge,o.service_id, " +
                            " s.platform,s.check_count,o.check_count_time,s.bonus, " +
                            " s.task,s.mode,s.bonus_check " +
                            "FROM order_running o " +
                            "LEFT JOIN profile_task a " +
                            " ON a.order_id = o.order_id AND a.running = 1 " +
                            "LEFT JOIN service s " +
                            " ON o.service_id = s.service_id " +
                            "WHERE o.username <> 'refill@gmail.com' " +
                            " AND o.username = (:user) " +
                            " AND o.start_time > 0 " +
                            "GROUP BY o.order_id",
            countQuery =
                    "SELECT COUNT(DISTINCT o.order_id) " +
                            "FROM order_running o " +
                            "WHERE o.username <> 'refill@gmail.com' " +
                            " AND o.username = (:user) " +
                            " AND o.start_time > 0",
            nativeQuery = true
    )
    Page<OrderRunningShow> getOrderRunningUser(
            @Param("user") String user,
            Pageable pageable
    );




    @Query(value = "Select o.order_id,o.order_key,o.order_link,count(running) as total_thread\n" +
            ",o.thread,o.priority,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.total_limit_time,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.charge,o.service_id,s.platform,s.check_count,o.check_count_time,s.bonus,\n" +
            "s.task,s.mode,s.bonus_check from order_running o \n" +
            "left join profile_task a on a.order_id=o.order_id and running=1 \n" +
            "left join service s on o.service_id=s.service_id where o.order_id=?1 \n" +
            "group by o.order_id order by o.start_time desc",nativeQuery = true)
    public OrderRunningShow get_Order_Running_By_OrderId(Long order_id);

    @Query(value = "Select o.order_id,o.order_key,o.order_link,0 as total_thread\n" +
            ",o.thread,o.priority,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.total_limit_time,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.charge,o.service_id,s.platform,s.check_count,o.check_count_time,s.bonus,\n" +
            "s.task,s.mode,s.bonus_check from order_running o \n" +
            "left join service s on o.service_id=s.service_id where  o.start_time=0 \n" +
            "group by o.order_id order by o.insert_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Pending();

    @Query(value = "Select o.order_id,o.order_key,o.order_link,0 as total_thread\n" +
            ",o.thread,o.priority,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.total_limit_time,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.charge,o.service_id,s.platform,s.check_count,o.check_count_time,s.bonus,\n" +
            "s.task,s.mode,s.bonus_check from order_running o \n" +
            "left join service s on o.service_id=s.service_id where o.username=?1 and  o.start_time=0 \n" +
            "group by o.order_id order by o.insert_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Pending(String username);

    @Query(value = "Select o.order_id,o.order_key,o.order_link,count(running) as total_thread\n" +
            ",o.thread,o.priority,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.total_limit_time,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.charge,o.service_id,s.platform,s.check_count,o.check_count_time,s.bonus,\n" +
            "s.task,s.mode,s.bonus_check from order_running o \n" +
            "left join profile_task a on a.order_id=o.order_id and running=1 \n" +
            "left join service s on o.service_id=s.service_id where o.username=?1 and  o.start_time>0 \n" +
            "group by o.order_id order by o.start_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Running(String username);

}
