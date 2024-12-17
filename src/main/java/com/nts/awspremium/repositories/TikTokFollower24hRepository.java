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
    @Query(value = "delete from tiktok_follower_24h where round((UNIX_TIMESTAMP()-update_time/1000)/60/60)>24;",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select count(*) from tiktok_follower_24h where id like ?1",nativeQuery = true)
    public Integer count_Follower_24h_By_Username(String username);

    @Query(value = "select count(*) from tiktok_follower_24h where id=?1",nativeQuery = true)
    public Integer check_Follower_24h_By_Username_And_TiktokId(String id);
}
