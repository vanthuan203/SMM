package com.nts.awspremium.repositories;

import com.nts.awspremium.model.FacebookComment24h;
import com.nts.awspremium.model.FacebookFollower24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface FacebookComment24hRepository extends JpaRepository<FacebookComment24h,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from facebook_comment_24h where round((UNIX_TIMESTAMP()-time/1000)/60/60)>24;",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select count(*) from facebook_comment_24h where id like ?1",nativeQuery = true)
    public Integer count_Comment_24h_By_Username(String username);
}
