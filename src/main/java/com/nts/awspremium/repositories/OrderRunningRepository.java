package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface OrderRunningRepository extends JpaRepository<OrderRunning,Long> {
    @Query(value = "SELECT * FROM order_running where service_id in(select service_id from service where platform=?1 and task=?2) and INSTR(?3,CONCAT(order_key,'|'))=0 and order_id in (?4) order by rand() limit 1",nativeQuery = true)
    public OrderRunning get_Order_Tiktok_By_Task(String platform,String task,String list_tiktok_id, List<String> order_id);

    @Query(value = "select order_id from (select order_running.order_id,count(running) as total,thread\n" +
            "                      from order_running left join account_task on account_task.order_id=order_running.order_id and running=1\n" +
            "                       group by order_id having total<thread) as t",nativeQuery = true)
    public List<String> get_List_Order_Thread_True();

    @Query(value = "SELECT * from order_running where order_id=?1",nativeQuery = true)
    public OrderRunning get_Order_By_Id(Long order_id);

    @Query(value = "SELECT count(*) from order_running where order_key=?1",nativeQuery = true)
    public Integer get_Order_By_Order_Key(String order_id);

    @Query(value = "SELECT * from order_running where order_id in (?1)",nativeQuery = true)
    public List<OrderRunning> get_Order_By_ListId(List<String> list_orderid);

}
