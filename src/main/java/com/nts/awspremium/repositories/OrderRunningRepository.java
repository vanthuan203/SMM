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
    public OrderRunning get_Order_Running_By_Task(String platform,String task,String mode,String list_tiktok_id, List<String> order_id);

    @Query(value = "SELECT * FROM order_running where start_time>0 and service_id in(select service_id from service where platform=?1 and task=?2 and mode=?3) and INSTR(?4,CONCAT(order_key,'|'))=0 and order_id in (?5) and priority>0 order by rand() limit 1",nativeQuery = true)
    public OrderRunning get_Order_Running_Priority_By_Task(String platform,String task,String mode,String list_tiktok_id, List<String> order_id);


    @Query(value = "select order_id from (select order_running.order_id,count(running) as total,thread\n" +
            "                      from order_running left join profile_task on profile_task.order_id=order_running.order_id and running=1\n" +
            "                       group by order_id having total<thread) as t",nativeQuery = true)
    public List<String> get_List_Order_Thread_True();

    @Query(value = "select order_id from (select order_running.order_id,count(running) as total,thread\n" +
            "                      from order_running left join profile_task on profile_task.order_id=order_running.order_id and running=1\n" +
            "                       group by order_id having total<2*thread) as t",nativeQuery = true)
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

    @Query("select o from OrderRunning o JOIN FETCH o.service where o.start_time>0 and ((:currentTime - o.update_time) >= :threshold or o.valid=0)")
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

    @Query(value = "SELECT SUM(thread_set) FROM Data.order_running where start_time>0 and service_id in(SELECT service_id FROM service where mode='auto');",nativeQuery = true)
    public Integer get_Sum_Thread_Order_Running_By_Mode_Auto();

    @Query(value = "SELECT count(*) FROM Data.order_running where service_id=?1 and start_time>0;",nativeQuery = true)
    public Integer get_Count_OrderRunning_By_Service(Integer service_id);

    @Query(value = "select o from OrderRunning o JOIN FETCH o.service where o.start_time=0 ORDER BY o.service.task ASC,o.priority DESC,o.insert_time ASC")
    public List<OrderRunning> get_Order_Pending_ASC();

    @Query(value = "SELECT service_id from order_running where order_key=?1 and service_id in(select service_id from service where task='view' and platform='youtube')",nativeQuery = true)
    public Integer get_ServiceId_By_TaskKey(String order_key);

    @Query(value = "SELECT count(*) from order_running where order_key=?1 and service_id in(select service_id from service where history=0)",nativeQuery = true)
    public Integer check_No_History(String order_key);
    @Query(value = "SELECT order_running.order_id,count(*) as total FROM history_sum left join order_running  on history_sum.order_id=order_running.order_id where order_running.start_time>0 group by order_running.order_id order by insert_time desc",nativeQuery = true)
    public List<String> get_Total_Buff_Cron();

    @Query(value = "select o from OrderRunning o JOIN FETCH o.service  where o.service.check_done=?1 and o.total>=(o.quantity+o.quantity*(o.service.bonus/100)) order by o.start_time asc")
    public List<OrderRunning> get_Order_Running_Done(Integer check_done);

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set update_time=IF(total<?1,?2,update_time),total=?1 where order_id=?3",nativeQuery = true)
    public void update_Total_Buff_By_OrderId(Integer total,Long update_time,Long order_id);

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
    @Query(value = "DELETE FROM order_running where order_id=?1",nativeQuery = true)
    public void delete_Order_Running_By_OrderId(Long order_id);

    @Query(value = "Select o.order_id,o.order_key,o.order_link,count(running) as total_thread\n" +
            ",o.thread,o.priority,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.charge,o.service_id,s.platform,s.check_count,o.check_count_time,s.bonus,\n" +
            "s.task,s.mode from order_running o \n" +
            "left join profile_task a on a.order_id=o.order_id and running=1 \n" +
            "left join service s on o.service_id=s.service_id where o.username!='refill@gmail.com' and  o.start_time>0 \n" +
            "group by o.order_id order by o.start_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Running();

    @Query(value = "Select o.order_id,o.order_key,o.order_link,count(running) as total_thread\n" +
            ",o.thread,o.priority,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.charge,o.service_id,s.platform,s.check_count,o.check_count_time,s.bonus,\n" +
            "s.task,s.mode from order_running o \n" +
            "left join profile_task a on a.order_id=o.order_id and running=1 \n" +
            "left join service s on o.service_id=s.service_id where o.order_id=?1 and o.start_time>0 \n" +
            "group by o.order_id order by o.start_time desc",nativeQuery = true)
    public OrderRunningShow get_Order_Running_By_OrderId(Long order_id);

    @Query(value = "Select o.order_id,o.order_key,o.order_link,0 as total_thread\n" +
            ",o.thread,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.charge,o.service_id,s.platform,s.check_count,o.check_count_time,s.bonus,\n" +
            "s.task,s.mode from order_running o \n" +
            "left join service s on o.service_id=s.service_id where  o.start_time=0 \n" +
            "group by o.order_id order by o.insert_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Pending();

    @Query(value = "Select o.order_id,o.order_key,o.order_link,0 as total_thread\n" +
            ",o.thread,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.charge,o.service_id,s.platform,s.check_count,o.check_count_time,s.bonus,\n" +
            "s.task,s.mode from order_running o \n" +
            "left join service s on o.service_id=s.service_id where o.username=?1 and  o.start_time=0 \n" +
            "group by o.order_id order by o.insert_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Pending(String username);

    @Query(value = "Select o.order_id,o.order_key,o.order_link,count(running) as total_thread\n" +
            ",o.thread,o.priority,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.charge,o.service_id,s.platform,s.check_count,o.check_count_time,s.bonus,\n" +
            "s.task,s.mode from order_running o \n" +
            "left join profile_task a on a.order_id=o.order_id and running=1 \n" +
            "left join service s on o.service_id=s.service_id where o.username=?1 and  o.start_time>0 \n" +
            "group by o.order_id order by o.start_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Running(String username);

}
