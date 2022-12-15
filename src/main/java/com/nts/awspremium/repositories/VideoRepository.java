package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoRepository extends JpaRepository<Video,Long> {
    @Query(value="SELECT * FROM video where INSTR(?1,videoid)=0 order by rand() limit 1",nativeQuery = true)
    public List<Video> getvideo1(String listvideo);
    @Query(value = "SELECT * FROM video where INSTR(?1,videoid)=0 and channelid in (select channelid from (select channel.channelid,count(*) as total,maxthreads from channel left join history on history.channelid=channel.channelid and running=1 where enabled=1 group by channelid having total<maxthreads) as t)  order by rand() limit 1",nativeQuery = true)
    public List<Video> getvideo(String listvideo);

    @Query(value = "SELECT * FROM video where INSTR(?1,videoid)=0 and channelid in (select channelid from (select channel.channelid,count(*) as total,maxthreads from channel left join history on history.channelid=channel.channelid and running=1 where enabled=1 group by channelid having total<maxthreads) as t)  order by rand() limit 1",nativeQuery = true)
    public List<Video> getvideotest(String listvideo);

    @Query(value = "SELECT * FROM video where INSTR(?1,videoid)=0 and channelid in (select channelid from (select channel.channelid,count(*) as total,maxthreads from channel left join history on history.channelid=channel.channelid and running=1 where enabled=1 group by channelid having total<maxthreads) as t) and channelid=?2 order by rand() limit 1",nativeQuery = true)
    public List<Video> getvideobychannel(String listvideo,String channelid);
    @Query(value = "SELECT * FROM video where INSTR(?1,videoid)=0 and videoid not in (select videoid from historyview where round((UNIX_TIMESTAMP()-time/1000)/60)<60  group by videoid having 30<=count(*)) and channelid in (select channelid from (select channel.channelid,count(*) as total,maxthreads from channel left join history on history.channelid=channel.channelid and running=1 where enabled=2 group by channelid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<Video> getvideobuff(String listvideo);

    @Query(value = "SELECT * FROM video where INSTR(?1,videoid)=0 and videoid not in \n" +
            "(select videoid from history where round((UNIX_TIMESTAMP()-timeget/1000)/60)<60 and running=1  group by videoid having 50<=count(videoid)) and \n" +
            "channelid in (select channelid from (select channel.channelid,count(*) as total,maxthreads \n" +
            "from channel left join history on history.channelid=channel.channelid and running=1 where enabled=1 \n" +
            "group by channelid having total<maxthreads) as t) \n" +
            "and channelid not in (select channelid from historysum where duration!=0 and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by channelid \n" +
            "having sum(duration)> 3500000  order by sum(duration) asc )  order by rand() limit 1",nativeQuery = true)
    public List<Video> getvideobuffh(String listvideo);

    @Query(value = "SELECT * FROM video where INSTR(?1,videoid)=0 and videoid not in \n" +
            "(select videoid from history where round((UNIX_TIMESTAMP()-timeget/1000)/60)<60 and running=1  group by videoid having 50<=count(videoid)) and \n" +
            "channelid in (select channelid from (select channel.channelid,count(*) as total,maxthreads \n" +
            "from channel left join history on history.channelid=channel.channelid and running=1 where enabled=?2 \n" +
            "group by channelid having total<maxthreads) as t) \n" +
            "and channelid not in (select channelid from historysum where duration!=0 and round((UNIX_TIMESTAMP()-time/1000)/60/60)<24 group by channelid \n" +
            "having sum(duration)> 3500000  order by sum(duration) asc )  order by rand() limit 1",nativeQuery = true)
    public List<Video> getvideobuffh(String listvideo,Integer enabled);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM video where channelid=?1",nativeQuery = true)
    public void deleteAllByChannelId(String channelid);
}
