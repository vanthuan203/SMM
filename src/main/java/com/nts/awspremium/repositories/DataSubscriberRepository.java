package com.nts.awspremium.repositories;

import com.nts.awspremium.model.DataComment;
import com.nts.awspremium.model.DataSubscriber;
import com.nts.awspremium.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DataSubscriberRepository extends JpaRepository<DataSubscriber,String> {

    @Query(value = "Select * from data_subscriber where order_id=?1 order by rand() limit 1",nativeQuery = true)
    public DataSubscriber get_Data_Subscriber(Long order_id);

    @Query(value = "Select o.order_key from data_subscriber d join order_running o on d.order_id=o.order_id  where d.video_id=?1 limit 1",nativeQuery = true)
    public String get_OrderKey_By_VideoId(String video_id);

}
