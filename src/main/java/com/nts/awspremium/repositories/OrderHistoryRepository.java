package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderHistory;
import com.nts.awspremium.model.OrderRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory,Long> {
    @Query(value = "SELECT * from order_history where order_id=?1",nativeQuery = true)
    public OrderHistory get_Order_By_Id(Long order_id);

    @Query(value = "SELECT * from order_history where order_id in (?1)",nativeQuery = true)
    public List<OrderHistory> get_Order_By_ListId(List<String> list_orderid);

}
