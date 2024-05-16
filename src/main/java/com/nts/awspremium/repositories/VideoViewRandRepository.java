package com.nts.awspremium.repositories;


import com.nts.awspremium.model.OrderViewRunning;
import com.nts.awspremium.model.VideoView;
import com.nts.awspremium.model.VideoViewRand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoViewRandRepository extends JpaRepository<VideoViewRand,Long> {
    @Query(value = "SELECT count(*) from videoviewrand where videoid=?1",nativeQuery = true)
    public Integer getCountVideoId(String videoid);

    @Query(value = "SELECT count(*) from videoviewrand where service in(select service from service where geo=?1)",nativeQuery = true)
    public Integer getCountVideoIdByGeo(String geo);

    @Query(value = "Select count(*) as total from videoviewrand left join historyview on historyview.videoid=videoviewrand.videoid  and running=1 where videoviewrand.service>600",nativeQuery = true)
    public Integer getCountThreadRunningVN();

    @Query(value = "Select count(*) as total from videoviewrand left join historyview on historyview.videoid=videoviewrand.videoid and running=1 where videoviewrand.service<600",nativeQuery = true)
    public Integer getCountThreadRunningUS();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM videoviewrand where round((UNIX_TIMESTAMP()-timestart/1000)/60/60)>=3",nativeQuery = true)
    public void deletevideoByTimeStart();


}
