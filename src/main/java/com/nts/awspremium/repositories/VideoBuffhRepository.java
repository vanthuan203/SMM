package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Video;
import com.nts.awspremium.model.VideoBuffh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoBuffhRepository extends JpaRepository<VideoBuffh,Long> {

    @Query(value = "SELECT videoid,sum(duration) as total,count(*) as view FROM historysum where videoid in (select videobuffh.videoid from videobuffh) group by videoid",nativeQuery = true)
    public List<String> getTimeBuffVideo();

    @Query(value = "SELECT videoid,sum(duration) as total,count(*) as view FROM historysum where round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 and videoid in (select videobuffh.videoid from videobuffh) group by videoid",nativeQuery = true)
    public List<String> getTimeBuff24hVideo();
    @Query(value = "SELECT * from videobuffh where videoid=?1",nativeQuery = true)
    public List<VideoBuffh> getVideoBuffhById(String vidoeid);
    @Query(value = "SELECT * FROM videobuffh where INSTR(?1,videoid)=0 and videoid not in \n" +
            "(select videoid from history where round((UNIX_TIMESTAMP()-timeget/1000)/60)<60 and running=1  group by videoid having 50<=count(videoid)) and \n" +
            "videoid in (select videoid from (select videobuffh.videoid,count(*) as total,maxthreads \n" +
            "from videobuffh left join history on history.videoid=videobuffh.videoid  where enabled=?2 \n" +
            "group by videoid having total<maxthreads) as t) \n" +
            "and videoid not in (select videoid from historysum where duration!=0 and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by videoid \n" +
            "having sum(duration)> 3500000  order by sum(duration) asc )  order by rand() limit 1",nativeQuery = true)
    public List<VideoBuffh> getvideobuffhVer2(String listvideo, Integer enabled);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM videobuffh where videoid=?1",nativeQuery = true)
    public void deletevideoByVideoId(String videoid);
}
