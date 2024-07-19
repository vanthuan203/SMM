package com.nts.awspremium.repositories;

import com.nts.awspremium.model.YoutubeComment24h;
import com.nts.awspremium.model.YoutubeLike24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface YoutubeComment24hRepository extends JpaRepository<YoutubeComment24h,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from youtube_comment_24h where round((UNIX_TIMESTAMP()-time/1000)/60/60)>24;",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select count(*) from youtube_comment_24h where id like ?1",nativeQuery = true)
    public Integer count_Comment_24h_By_Username(String username);
}
