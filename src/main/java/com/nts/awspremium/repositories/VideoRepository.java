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
    @Query(value = "SELECT * FROM video where INSTR(?1,videoid)=0 and channelid in (select channelid from (select channel.channelid,count(*) as total,maxthreads from channel left join history on history.channelid=channel.channelid and running=1 where enabled=1 group by channelid having total<maxthreads) as t) and channelid!='UCoxzZ-HayM5Y9_9h5GDWmgQ' order by rand() limit 1",nativeQuery = true)
    public List<Video> getvideo(String listvideo);

    @Query(value = "SELECT * FROM video where INSTR(?1,videoid)=0 and channelid in (select channelid from (select channel.channelid,count(*) as total,maxthreads from channel left join history on history.channelid=channel.channelid and running=1 where enabled=1 group by channelid having total<maxthreads) as t) and channelid='UCoxzZ-HayM5Y9_9h5GDWmgQ' order by rand() limit 1",nativeQuery = true)
    public List<Video> getvideotest(String listvideo);
    @Query(value = "SELECT * FROM video where INSTR(?1,videoid)=0 and channelid in (select channelid from (select channel.channelid,count(*) as total,maxthreads from channel left join history on history.channelid=channel.channelid and running=1 where enabled=2 group by channelid having total<maxthreads) as t) order by rand() limit 1",nativeQuery = true)
    public List<Video> getvideobuff(String listvideo);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM video where channelid=?1",nativeQuery = true)
    public void deleteAllByChannelId(String channelid);
}
