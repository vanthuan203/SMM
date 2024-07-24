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
    @Query(value = "delete from youtube_view_24h where round((UNIX_TIMESTAMP()-update_time/1000)/60/60)>24;",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select count(*) from youtube_view_24h where id like ?1",nativeQuery = true)
    public Integer count_View_24h_By_Username(String username);
}
