package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderBuffhRunning;
import com.nts.awspremium.model.OrderRunning;
import com.nts.awspremium.model.VideoBuffh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderBuffhRunningRepository extends JpaRepository<VideoBuffh,Long> {
    @Query(value = "Select videobuffh.videoid,videobuffh.videotitle,count(*) as total,maxthreads,timebuff,insertdate,enabled,note,duration,optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate,viewstart from videobuffh left join history on history.videoid=videobuffh.videoid and running=1 where enabled!=0   group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderBuffhRunning> getOrder();

    @Query(value = "Select videobuffh.videoid,videobuffh.videotitle,count(*) as total,\n" +
            "maxthreads,timebuff,insertdate,enabled,note,duration,optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate,viewstart\n" +
            "from videobuffh left join history on history.videoid=videobuffh.videoid and running=1 where CONCAT(videobuffh.videoid,'-',videobuffh.videotitle,'-',\n" +
            "maxthreads,'-',timebuff,'-',insertdate,'-',note,'-',optionbuff) like ?1 and enabled!=0   group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderBuffhRunning> getOrderFilter(String key);

    @Query(value = "select videobuffh.videoid,videobuffh.videotitle,0,maxthreads,timebuff,insertdate,enabled,note,videobuffh.duration,\n" +
            "            optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate from videobuffh left join\n" +
            "            historysum on videobuffh.videoid=historysum.videoid where enabled!=0 and videobuffh.insertdate<=historysum.time group by videobuffh.videoid having sum(historysum.duration)>(3600*videobuffh.timebuff + videobuffh.timebuff*0.05*3600)",nativeQuery = true)
    public List<OrderBuffhRunning> getOrderFullBuffh();

    @Query(value = "Select videobuffh.videoid,videobuffh.videotitle,0 as total,maxthreads,timebuff,insertdate,enabled,note,duration," +
            "optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate from videobuffh group by videoid order by insertdate desc limit ?1",nativeQuery = true)
    public List<OrderBuffhRunning> getOrderNewAdd(Integer limit);

    @Query(value = "Select videobuffh.videoid,videobuffh.videotitle,count(*) as total,maxthreads,timebuff,insertdate,enabled,note,duration,optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate from videobuffh left join history on history.videoid=videobuffh.videoid and running=1 where enabled!=0 and videobuffh.videoid=?1",nativeQuery = true)
    public List<OrderBuffhRunning> getVideoBuffhById(String vidoeid);
}
