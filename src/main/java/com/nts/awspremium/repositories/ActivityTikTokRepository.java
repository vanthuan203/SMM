package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ActivityTikTok;
import com.nts.awspremium.model.AuthenIPv4;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ActivityTikTokRepository extends JpaRepository<ActivityTikTok,Long> {
    @Query(value = "SELECT count(*) FROM activity_tiktok where round((UNIX_TIMESTAMP()-time_update/1000)/60/60)<(24/(SELECT max_activity_24h FROM setting_tiktok where id=1)) and username=?1",nativeQuery = true)
    public Integer checkActivityByUsername(String username);

}
