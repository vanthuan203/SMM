package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ActivityTikTok;
import com.nts.awspremium.model.AuthenIPv4;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ActivityTikTokRepository extends JpaRepository<ActivityTikTok,Long> {
    @Query(value = "SELECT count(*) FROM activity_tiktok where round((UNIX_TIMESTAMP()-time_update/1000)/60/60)<24 and username=?1",nativeQuery = true)
    public Integer checkActivityByUsername(String username);

    @Query(value = "Select ipv4 from authenipv4",nativeQuery = true)
    public List<String> getAuthen();

    @Modifying
    @Transactional
    @Query(value = "delete from authenipv4 where ipv4=?1",nativeQuery = true)
    public Integer deleteAuthenByIPV4(String ipv4);

    @Query(value = "Select ipv4,timecheck,lockmode from authenipv4 order by timeadd desc",nativeQuery = true)
    public List<String> getListAuthen();
}
