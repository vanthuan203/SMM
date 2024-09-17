package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderHistory;
import com.nts.awspremium.model.OrderHistoryShow;
import com.nts.awspremium.model.OrderRunning;
import com.nts.awspremium.model.OrderRunningShow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory,Long> {
    @Query(value = "SELECT * from order_history where order_id=?1",nativeQuery = true)
    public OrderHistory get_Order_By_Id(Long order_id);

    @Query(value = "SELECT end_time from order_history where order_refill=?1 and cancel!=1 order by end_time desc limit 1",nativeQuery = true)
    public default Long get_End_Time_By_Order_Refill(Long order_refill){
        Long end_time = order_refill.longValue();
        if(end_time==null){
            return 0L;
        }
        return end_time;
    }
    @Query(value = "SELECT end_time from order_history where order_key=?1 and order_refill=0 and cancel!=1 order by end_time desc limit 1",nativeQuery = true)
    public default Long get_End_Time_By_Order_Key(Long order_refill){
        Long end_time = order_refill.longValue();
        if(end_time==null){
            return 0L;
        }
        return end_time;
    }

    @Query(value = "SELECT * from order_history where order_id in (?1)",nativeQuery = true)
    public List<OrderHistory> get_Order_By_ListId(List<String> list_orderid);
    @Query(value = "SELECT GROUP_CONCAT(order_key) from (SELECT order_key FROM Data.order_history where service_id in(select service_id from service where platform=?1 and task=?2) \n" +
            "and round((UNIX_TIMESTAMP()-end_time/1000)/60/60)>=8 and\n" +
            " round((UNIX_TIMESTAMP()-end_time/1000)/60/60)<24 and\n" +
            " update_current_time=0 and valid!=0 and cancel!=1  order by end_time asc limit 35) as oh",nativeQuery = true)
    public String get_List_OrderKey_CheckCount8h(String platform,String task);

    @Modifying
    @Transactional
    @Query(value = "update order_history set current_count=?1,update_current_time=?2,mode_check=?3 where order_key=?4 and service_id in(select service_id from service where platform=?5 and task=?6) and round((UNIX_TIMESTAMP()-end_time/1000)/60/60)>=8 and round((UNIX_TIMESTAMP()-end_time/1000)/60/60)<24",nativeQuery = true)
    public void update_Order_CheckCount(Integer current_count,Long update_current_time,String mode_check,String order_key,String platform,String task);

    @Modifying
    @Transactional
    @Query(value = "update order_history set valid=0,update_time=?1 where order_key in (?2) and service_id in(select service_id from service where platform=?3 and task=?4)",nativeQuery = true)
    public void update_Order_NotValid(Long update_time, List<String> order_key,String platform,String task);
    @Query(value = "Select o.order_id,o.order_key,o.order_link,o.insert_time,o.start_time,o.end_time,o.cancel,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.refund,o.refund_time,o.refill,o.refill_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task,s.mode from order_history o left join service s on o.service_id=s.service_id where o.order_key in (?1) or o.order_id in (?1)\n" +
            "order by o.end_time desc",nativeQuery = true)
    public List<OrderHistoryShow> get_Order_History_By_Key(List<String> key);


    @Query(value = "Select o.order_id,o.order_key,o.order_link,o.insert_time,o.start_time,o.end_time,o.cancel,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.refund,o.refund_time,o.refill,o.refill_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task,s.mode from order_history o left join service s on o.service_id=s.service_id where  (o.order_key in (?1) or o.order_id in (?1)) and o.username=?2 \n" +
            "order by o.end_time desc",nativeQuery = true)
    public List<OrderHistoryShow> get_Order_History_By_Key(List<String> key,String user);

    @Query(value = "Select o.order_id,o.order_key,o.order_link,o.insert_time,o.start_time,o.end_time,o.cancel,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.refund,o.refund_time,o.refill,o.refill_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task,s.mode from order_history o left join service s on o.service_id=s.service_id\n" +
            "order by o.end_time desc",nativeQuery = true)
    public List<OrderHistoryShow> get_Order_History();



    @Query(value = "Select o.order_id,o.order_key,o.order_link,o.insert_time,o.start_time,o.end_time,o.cancel,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.refund,o.refund_time,o.refill,o.refill_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task,s.mode from order_history o left join service s on o.service_id=s.service_id where  o.username=?1 \n" +
            "order by o.end_time desc",nativeQuery = true)
    public List<OrderHistoryShow> get_Order_History(String username);

    @Query(value = "Select o.order_id,o.order_key,o.order_link,o.insert_time,o.start_time,o.end_time,o.cancel,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.update_current_time,o.refund,o.refund_time,o.refill,o.refill_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task,s.mode from order_history o left join service s on o.service_id=s.service_id where  o.order_id=?1 \n" +
            "limit 1",nativeQuery = true)
    public OrderHistoryShow get_Order_History(Long order_id);

}
