package com.nts.awspremium.repositories;

import com.nts.awspremium.model.DataSubscriber;
import com.nts.awspremium.model.OrderComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderCommentRepository extends JpaRepository<OrderComment,Long> {

    @Query(value = "Select * from order_comment where order_id=?1 limit 1",nativeQuery = true)
    public OrderComment get_OrderComment_By_OrderId(Long order_id);

}
