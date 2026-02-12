package com.nts.awspremium.repositories;

import com.nts.awspremium.model.YoutubeSubscriber24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface YoutubeSubscriber24hRepository extends JpaRepository<YoutubeSubscriber24h,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from youtube_subscriber_24h where update_time <= (UNIX_TIMESTAMP() - 240*60*60) * 1000",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select count(*) from youtube_subscriber_24h where id like ?1 and update_time <= (UNIX_TIMESTAMP() - 24*60*60) * 1000",nativeQuery = true)
    public Integer count_Subscribe_24h_By_Username(String username);

    @Query(value = "SELECT COUNT(DISTINCT device_id) FROM youtube_subscriber_24h WHERE order_key = ?1",nativeQuery = true)
    public Integer count_Subscribe_DeviceId_By_OrderKey(String order_key);

    @Query(value = "select count(*) from youtube_subscriber_24h where device_id=?1",nativeQuery = true)
    public Integer count_Subscribe_24h_By_DeviceId_And_OrderKey(String device_id);
}
