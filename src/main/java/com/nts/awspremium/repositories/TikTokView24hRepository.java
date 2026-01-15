package com.nts.awspremium.repositories;

import com.nts.awspremium.model.TiktokComment24h;
import com.nts.awspremium.model.TiktokView24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface TikTokView24hRepository extends JpaRepository<TiktokView24h,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from tiktok_view_24h where update_time < (UNIX_TIMESTAMP() - 24*3600) * 1000",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select count(*) from tiktok_view_24h where id like ?1",nativeQuery = true)
    public Integer count_View_24h_By_Username(String username);
}
