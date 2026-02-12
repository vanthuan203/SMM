package com.nts.awspremium.repositories;

import com.nts.awspremium.model.YoutubeLike24h;
import com.nts.awspremium.model.YoutubeView24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface YoutubeView24hRepository extends JpaRepository<YoutubeView24h,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from youtube_view_24h where update_time <= (UNIX_TIMESTAMP() - 24*60*60) * 1000",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select count(*) from youtube_view_24h where id like ?1",nativeQuery = true)
    public Integer count_View_24h_By_Username(String username);
    @Query(value = "select count(*) from youtube_view_24h where device_id=?1 and update_time >= (UNIX_TIMESTAMP() - ?2*60*60) * 1000",nativeQuery = true)
    public Integer count_View_By_DeviceId_And_OrderKey_And_Time(String device_id,Integer hour);
    @Query(value = "SELECT COUNT(DISTINCT device_id) FROM youtube_view_24h WHERE order_key = ?1 and update_time >= (UNIX_TIMESTAMP() - ?2*60*60) * 1000",nativeQuery = true)
    public Integer count_View_DeviceId_By_OrderKey(String order_key,Integer hour);

    @Query(value = "select count(*) from youtube_view_24h where device_id=?1 and update_time >= (UNIX_TIMESTAMP() - ?2*60*60) * 1000",nativeQuery = true)
    public Integer count_View_By_DeviceId_And_Hour(String device_id,Integer hour);
}
