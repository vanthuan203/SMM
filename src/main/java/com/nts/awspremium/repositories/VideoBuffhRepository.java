package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Video;
import com.nts.awspremium.model.VideoBuffh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoBuffhRepository extends JpaRepository<VideoBuffh,Long> {

    @Query(value = "SELECT videobuffh.videoid,sum(historysum.duration) as total,count(*) as view FROM historysum left join videobuffh on historysum.videoid=videobuffh.videoid where videobuffh.enabled!=0 and time>=videobuffh.insertdate group by videobuffh.videoid order by insertdate desc",nativeQuery = true)
    public List<String> getTimeBuffVideo();

    @Query(value = "SELECT videobuffh.videoid,sum(historysum.duration) as total,count(*) as view FROM historysum left join videobuffh on historysum.videoid=videobuffh.videoid where videobuffh.enabled!=0 and videobuffh.user=?1 and time>=videobuffh.insertdate group by videobuffh.videoid order by insertdate desc",nativeQuery = true)
    public List<String> getTimeBuffVideo(String user);

    @Query(value = "SELECT videobuffh.videoid,sum(historysum.duration) as total,count(*) as view FROM historysum left join videobuffh on historysum.videoid=videobuffh.videoid where videobuffh.enabled!=0 and time>=videobuffh.insertdate and videobuffh.videoid=?1  group by videobuffh.videoid order by insertdate desc limit 1",nativeQuery = true)
    public String getTimeBuffByVideoId(String videoid);

    @Query(value = "SELECT videobuffh.videoid,sum(historysum.duration) as total,count(*) as view FROM historysum left join videobuffh on historysum.videoid=videobuffh.videoid where videobuffh.enabled!=0 and time>=videobuffh.insertdate and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by videobuffh.videoid order by insertdate desc",nativeQuery = true)
    public List<String> getTimeBuff24hVideo();

    @Query(value = "SELECT videobuffh.videoid,sum(historysum.duration) as total,count(*) as view FROM historysum left join videobuffh on historysum.videoid=videobuffh.videoid where videobuffh.enabled!=0 and videobuffh.user=?1 and time>=videobuffh.insertdate and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by videobuffh.videoid order by insertdate desc",nativeQuery = true)
    public List<String> getTimeBuff24hVideo(String user);

    @Query(value = "SELECT videobuffh.videoid,sum(historysum.duration) as total,count(*) as view FROM historysum left join videobuffh on historysum.videoid=videobuffh.videoid where videobuffh.enabled!=0 and time>=videobuffh.insertdate and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 and videobuffh.videoid=?1 group by videobuffh.videoid order by insertdate desc limit 1",nativeQuery = true)
    public String getTimeBuff24hByVideoId(String videoid);
    @Query(value = "SELECT * from videobuffh where enabled!=0 and videoid=?1",nativeQuery = true)
    public List<VideoBuffh> getVideoBuffhById(String vidoeid);

    @Query(value = "SELECT count(*) from videobuffh where videoid=?1 and enabled!=0",nativeQuery = true)
    public Integer getCountVideoId(String vidoeid);
    @Query(value = "SELECT sum(timebuff) as total FROM AccPremium.videobuffh where enabled!=0",nativeQuery = true)
    public Integer getCountTimeBuffOrder();

    @Query(value = "SELECT sum(timebuff) as total FROM AccPremium.videobuffh where enabled!=0 and user=?1",nativeQuery = true)
    public Integer getCountTimeBuffOrder(String user);

    @Query(value = "SELECT Round(sum(historysum.duration)/3600,0) as timebuff FROM historysum left join videobuffh on historysum.videoid=videobuffh.videoid where videobuffh.enabled!=0 and time>=videobuffh.insertdate",nativeQuery = true)
    public Integer getCountTimeBuffedOrder();

    @Query(value = "SELECT Round(sum(historysum.duration)/3600,0) as timebuff FROM historysum left join videobuffh on historysum.videoid=videobuffh.videoid where videobuffh.enabled!=0 and videobuffh.user=?1 and time>=videobuffh.insertdate",nativeQuery = true)
    public Integer getCountTimeBuffedOrder(String user);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM videobuffh where videoid=?1",nativeQuery = true)
    public void DeleteVideoBuffhDone(String videoid);
    @Query(value = "SELECT * FROM videobuffh where INSTR(?1,videoid)=0 and videoid not in \n" +
            "(select videoid from history where round((UNIX_TIMESTAMP()-timeget/1000)/60)<60 and running=1  group by videoid having 50<=count(videoid)) and \n" +
            "videoid in (select videoid from (select videobuffh.videoid,count(*) as total,maxthreads \n" +
            "from videobuffh left join history on history.videoid=videobuffh.videoid and running=1  where enabled=?2 \n" +
            "group by videoid having total<maxthreads) as t) \n" +
            "and videoid not in (select videoid from historysum where duration!=0 and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by videoid \n" +
            "having sum(duration)> 3500000  order by sum(duration) asc )  order by rand() limit 1",nativeQuery = true)
    public List<VideoBuffh> getvideobuffhVer2(String listvideo, Integer enabled);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM videobuffh where enabled!=0  and videoid=?1",nativeQuery = true)
    public void deletevideoByVideoId(String videoid);
}
