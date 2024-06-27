package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderRunning;
import com.nts.awspremium.model.OrderRunningShow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface OrderRunningRepository extends JpaRepository<OrderRunning,Long> {
    @Query(value = "SELECT * FROM order_running where service_id in(select service_id from service where platform=?1 and task=?2) and INSTR(?3,CONCAT(order_key,'|'))=0 and order_id in (?4) order by rand() limit 1",nativeQuery = true)
    public OrderRunning get_Order_Running_By_Task(String platform,String task,String list_tiktok_id, List<String> order_id);

    @Query(value = "select order_id from (select order_running.order_id,count(running) as total,thread\n" +
            "                      from order_running left join account_task on account_task.order_id=order_running.order_id and running=1\n" +
            "                       group by order_id having total<thread) as t",nativeQuery = true)
    public List<String> get_List_Order_Thread_True();

    @Query(value = "SELECT * from order_running where order_id=?1",nativeQuery = true)
    public OrderRunning get_Order_By_Id(Long order_id);

    @Query(value = "SELECT count(*) from order_running where order_key=?1",nativeQuery = true)
    public Integer get_Order_By_Order_Key(String order_id);

    @Query(value = "SELECT o from OrderRunning o JOIN FETCH o.service where o.service.service_id in(select o.service.service_id from Service where check_count=1) and o.total>0 and o.start_time>0")
    public List<OrderRunning> get_Order_By_Check_Count();
    @Query(value = "SELECT count(*) from order_running where order_key=?1 and service_id in(select service_id from service where task=?2)",nativeQuery = true)
    public Integer get_Order_By_Order_Key_And_Task(String order_id,String task);

    @Query(value = "SELECT * from order_running where order_key=?1 and  service_id in(select service_id from service where task=?2 and platform=?3) limit 1",nativeQuery = true)
    public OrderRunning find_Order_By_Order_Key(String order_key,String task,String platform);

    @Query(value = "SELECT * from order_running where order_id in (?1)",nativeQuery = true)
    public List<OrderRunning> get_Order_By_ListId(List<String> list_orderid);

    @Query(value = "SELECT o FROM OrderRunning o where o.service.task='comment' and o.start_time=0 limit 15")
    public List<OrderRunning> get_Order_Comment_Pending();

    @Query(value = "SELECT order_running.order_id,count(*) as total FROM history_sum left join order_running  on history_sum.order_id=order_running.order_id where order_running.start_time>0 group by order_running.order_id order by insert_time desc",nativeQuery = true)
    public List<String> get_Total_Buff_Cron();

    @Query(value = "select o.* from order_running o join service s on o.service_id=s.service_id and s.check_done=?1 where o.total>=(o.quantity+o.quantity*s.bonus) order by start_time desc",nativeQuery = true)
    public List<OrderRunning> get_Order_Running_Done(Integer check_done);

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_running set total=?1,update_time=?2 where order_id=?3",nativeQuery = true)
    public void update_Total_Buff_By_OrderId(Integer total,Long update_time,Long order_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM order_running where order_id=?1",nativeQuery = true)
    public void delete_Order_Running_By_OrderId(Long order_id);

    @Query(value = "Select o.order_id,o.order_key,count(running) as total_thread\n" +
            ",o.thread,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task from order_running o \n" +
            "left join account_task a on a.order_id=o.order_id and running=1 \n" +
            "left join service s on o.service_id=s.service_id where  o.start_time>0 \n" +
            "group by o.order_id order by o.start_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Running();

    @Query(value = "Select o.order_id,o.order_key,0 as total_thread\n" +
            ",o.thread,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task from order_running o \n" +
            "left join service s on o.service_id=s.service_id where  o.start_time=0 \n" +
            "group by o.order_id order by o.insert_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Pending();

    @Query(value = "Select o.order_id,o.order_key,0 as total_thread\n" +
            ",o.thread,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task from order_running o \n" +
            "left join service s on o.service_id=s.service_id where o.username=?1 and  o.start_time=0 \n" +
            "group by o.order_id order by o.insert_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Pending(String username);

    @Query(value = "Select o.order_id,o.order_key,count(running) as total_thread\n" +
            ",o.thread,o.insert_time,o.start_time,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task from order_running o \n" +
            "left join account_task a on a.order_id=o.order_id and running=1 \n" +
            "left join service s on o.service_id=s.service_id where o.username=?1 and  o.start_time>0 \n" +
            "group by o.order_id order by o.start_time desc",nativeQuery = true)
    public List<OrderRunningShow> get_Order_Running(String username);

}
