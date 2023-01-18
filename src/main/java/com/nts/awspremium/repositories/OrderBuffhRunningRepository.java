package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderBuffhRunning;
import com.nts.awspremium.model.OrderRunning;
import com.nts.awspremium.model.VideoBuffh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderBuffhRunningRepository extends JpaRepository<VideoBuffh,Long> {
    @Query(value = "Select videobuffh.videoid,videobuffh.videotitle,count(*) as total,maxthreads,timebuff,insertdate,enabled,note,duration,optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate,viewstart,user,timebufftotal,viewtotal,timeupdate,timebuff24h,view24h,price from videobuffh left join history on history.videoid=videobuffh.videoid and running=1 where enabled!=0   group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderBuffhRunning> getOrder();

    @Query(value = "Select videobuffh.videoid,videobuffh.videotitle,count(*) as total,maxthreads,timebuff,insertdate,enabled,note,duration,optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate,viewstart,user,timebufftotal,viewtotal,timeupdate,timebuff24h,view24h,price from videobuffh left join history on history.videoid=videobuffh.videoid and running=1 where enabled!=0  and user=?1  group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderBuffhRunning> getOrder(String user);

    @Query(value = "Select videobuffh.videoid,videobuffh.videotitle,count(*) as total,\n" +
            "maxthreads,timebuff,insertdate,enabled,note,duration,optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate,viewstart,user\n" +
            "from videobuffh left join history on history.videoid=videobuffh.videoid and running=1 where CONCAT(videobuffh.videoid,'-',videobuffh.videotitle,'-',\n" +
            "maxthreads,'-',timebuff,'-',insertdate,'-',note,'-',optionbuff) like ?1 and enabled!=0   group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderBuffhRunning> getOrderFilter(String key);

    @Query(value = "Select videobuffh.videoid,videobuffh.videotitle,count(*) as total,\n" +
            "maxthreads,timebuff,insertdate,enabled,note,duration,optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate,viewstart,user\n" +
            "from videobuffh left join history on history.videoid=videobuffh.videoid and running=1 where CONCAT(videobuffh.videoid,'-',videobuffh.videotitle,'-',\n" +
            "maxthreads,'-',timebuff,'-',insertdate,'-',note,'-',optionbuff) like ?1 and enabled!=0 and videobuffh.user=?2  group by videoid order by insertdate desc",nativeQuery = true)
    public List<OrderBuffhRunning> getOrderFilter(String key,String user);

    @Query(value = "select * from videobuffh where timebufftotal>(3600*timebuff + timebuff*(select bonus/100 from setting where id=1)*3600) and (CASE WHEN duration<3600 THEN viewtotal>=(timebuff+ timebuff*(select bonus/100 from setting where id=1))*2  WHEN duration<7200 THEN viewtotal>=(timebuff+ timebuff*(select bonus/100 from setting where id=1)) ELSE viewtotal>=(timebuff+ timebuff*(select bonus/100 from setting where id=1))/2 END)",nativeQuery = true)
    public List<VideoBuffh> getOrderFullBuffh();

    @Query(value = "Select videobuffh.videoid,videobuffh.videotitle,0 as total,maxthreads,timebuff,insertdate,enabled,note,duration," +
            "optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate,user from videobuffh group by videoid order by insertdate desc limit ?1",nativeQuery = true)
    public List<OrderBuffhRunning> getOrderNewAdd(Integer limit);

    @Query(value = "Select videobuffh.videoid,videobuffh.videotitle,count(*) as total,maxthreads,timebuff,viewstart,insertdate,enabled,note,duration,optionbuff,mobilerate,searchrate,suggestrate,directrate,homerate,likerate,commentrate,user,timebufftotal,viewtotal,timeupdate,timebuff24h,view24h from videobuffh left join history on history.videoid=videobuffh.videoid and running=1 where enabled!=0 and videobuffh.videoid=?1",nativeQuery = true)
    public List<OrderBuffhRunning> getVideoBuffhById(String vidoeid);
}
