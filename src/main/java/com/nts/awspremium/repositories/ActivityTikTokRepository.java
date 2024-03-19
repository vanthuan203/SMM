package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ActivityTikTok;
import com.nts.awspremium.model.AuthenIPv4;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ActivityTikTokRepository extends JpaRepository<ActivityTikTok,Long> {
    @Query(value = "Select count(*) from authenipv4 where ipv4=?1",nativeQuery = true)
    public Integer CheckIPv4Exist(String ipv4);

    @Query(value = "Select ipv4 from authenipv4",nativeQuery = true)
    public List<String> getAuthen();

    @Modifying
    @Transactional
    @Query(value = "delete from authenipv4 where ipv4=?1",nativeQuery = true)
    public Integer deleteAuthenByIPV4(String ipv4);

    @Query(value = "Select ipv4,timecheck,lockmode from authenipv4 order by timeadd desc",nativeQuery = true)
    public List<String> getListAuthen();
}
