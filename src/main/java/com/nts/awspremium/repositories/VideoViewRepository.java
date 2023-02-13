package com.nts.awspremium.repositories;


import com.nts.awspremium.model.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VideoViewRepository extends JpaRepository<VideoView,Long> {

    @Query(value = "SELECT * from videoview where orderid in (?1)",nativeQuery = true)
    public List<VideoView> getVideoViewByListId(String list_orderid);

    @Query(value = "SELECT count(*) from videoview where enabled!=0 and user=?1",nativeQuery = true)
    public Integer getCountOrderByUser(String user);

    @Query(value = "SELECT count(*) from videoview where videoid=?1",nativeQuery = true)
    public Integer getCountVideoId(String videoid);
}
