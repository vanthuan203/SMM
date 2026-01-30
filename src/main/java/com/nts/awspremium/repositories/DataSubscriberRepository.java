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

    @Query(value = "Select * from data_subscriber where order_id=?1  order by state desc,task_time asc limit 1",nativeQuery = true)
    public DataSubscriber get_Data_Subscriber_By_State(Long order_id);

    @Query(value = "Select * from data_subscriber where order_id=?1 and state=1 and task_time  >= (UNIX_TIMESTAMP() - 6*60*60) * 1000 order by rand() limit 1",nativeQuery = true)
    public DataSubscriber get_Data_Subscriberf(Long order_id);

    @Query(value = "Select channel_id from data_subscriber where video_id=?1 limit 1",nativeQuery = true)
    public String get_ChannelId_By_VideoId(String video_id);

}
