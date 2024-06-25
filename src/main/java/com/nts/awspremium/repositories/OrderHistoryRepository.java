package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderHistory;
import com.nts.awspremium.model.OrderHistoryShow;
import com.nts.awspremium.model.OrderRunning;
import com.nts.awspremium.model.OrderRunningShow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory,Long> {
    @Query(value = "SELECT * from order_history where order_id=?1",nativeQuery = true)
    public OrderHistory get_Order_By_Id(Long order_id);

    @Query(value = "SELECT * from order_history where order_id in (?1)",nativeQuery = true)
    public List<OrderHistory> get_Order_By_ListId(List<String> list_orderid);


    @Query(value = "Select o.order_id,o.order_key,o.insert_time,o.start_time,o.end_time,o.cancel,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task from order_history o left join service s on o.service_id=s.service_id where o.order_key in (?1) or o.order_id in (?1)\n" +
            "order by o.end_time desc",nativeQuery = true)
    public List<OrderHistoryShow> get_Order_History_By_Key(List<String> key);


    @Query(value = "Select o.order_id,o.order_key,o.insert_time,o.start_time,o.end_time,o.cancel,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task from order_history o left join service s on o.service_id=s.service_id where  (o.order_key in (?1) or o.order_id in (?1)) and o.username=?2 \n" +
            "order by o.end_time desc",nativeQuery = true)
    public List<OrderHistoryShow> get_Order_History_By_Key(List<String> key,String user);

    @Query(value = "Select o.order_id,o.order_key,o.insert_time,o.start_time,o.end_time,o.cancel,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task from order_history o left join service s on o.service_id=s.service_id\n" +
            "order by o.end_time desc",nativeQuery = true)
    public List<OrderHistoryShow> get_Order_History();



    @Query(value = "Select o.order_id,o.order_key,o.insert_time,o.start_time,o.end_time,o.cancel,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task from order_history o left join service s on o.service_id=s.service_id where  o.username=?1 \n" +
            "order by o.end_time desc",nativeQuery = true)
    public List<OrderHistoryShow> get_Order_History(String username);

    @Query(value = "Select o.order_id,o.order_key,o.insert_time,o.start_time,o.end_time,o.cancel,o.note,\n" +
            "o.start_count,o.quantity,o.username,o.total,o.current_count,\n" +
            "o.update_time,o.charge,o.service_id,s.platform,s.check_count,s.bonus,\n" +
            "s.task from order_history o left join service s on o.service_id=s.service_id where  o.order_id=?1 \n" +
            "limit 1",nativeQuery = true)
    public OrderHistoryShow get_Order_History(Long order_id);

}
