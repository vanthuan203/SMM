package com.nts.awspremium.repositories;

import com.nts.awspremium.model.DataFollowerTiktok;
import com.nts.awspremium.model.DataSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface DataFollowerTiktokRepository extends JpaRepository<DataFollowerTiktok,String> {

    @Query(value = "Select * from data_follower_tiktok where order_id=?1 order by rand() limit 1",nativeQuery = true)
    public DataFollowerTiktok get_Data_Follower(Long order_id);

    @Query(value = "Select tiktok_id from data_follower_tiktok where video_id=?1 limit 1",nativeQuery = true)
    public String get_TiktokId_By_VideoId(String video_id);

    @Modifying
    @Transactional
    @Query(value = "delete from data_follower_tiktok where order_id=?1",nativeQuery = true)
    public Integer delete_Data_Follower_By_OrderId(Long order_id);

}
