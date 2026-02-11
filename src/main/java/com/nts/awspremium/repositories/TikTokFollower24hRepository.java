package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.TiktokFollower24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface TikTokFollower24hRepository extends JpaRepository<TiktokFollower24h,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from tiktok_follower_24h where update_time <= (UNIX_TIMESTAMP() - 24*60*60) * 1000",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select count(*) from tiktok_follower_24h where id like ?1",nativeQuery = true)
    public Integer count_Follower_24h_By_Username(String username);
    @Query(value = "select count(*) from tiktok_follower_24h where device_id=?1",nativeQuery = true)
    public Integer count_Follower_24h_By_DeviceId(String device_id);
    @Query(value = "select count(*) from tiktok_follower_24h where id like ?1 and update_time >= (UNIX_TIMESTAMP() - 60*60) * 1000",nativeQuery = true)
    public Integer count_Follower_1h_By_Username(String username);

    @Query(value = "select count(*) from tiktok_follower_24h where id=?1",nativeQuery = true)
    public Integer check_Follower_24h_By_Username_And_TiktokId(String id);
    @Query(value = "SELECT count(*) FROM Data.tiktok_follower_24h;",nativeQuery = true)
    public Integer check_Follower_24h();
}
