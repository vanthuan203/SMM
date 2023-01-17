package com.nts.awspremium.repositories;

import com.nts.awspremium.model.OrderBuffhRunning;
import com.nts.awspremium.model.VideoBuffh;
import com.nts.awspremium.model.VideoBuffhHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VideoBuffhHistoryRepository extends JpaRepository<VideoBuffhHistory,Long> {

    @Query(value = "SELECT * from videobuffhhistory order by enddate desc",nativeQuery = true)
    public List<VideoBuffhHistory> getVideoBuffhHistories();

    @Query(value = "SELECT * from videobuffhhistory where user=?1 order by enddate desc",nativeQuery = true)
    public List<VideoBuffhHistory> getVideoBuffhHistories(String user);

    @Query(value = "Select *\n" +
            "from videobuffhhistory where CONCAT(videoid,'-',videotitle,'-',\n" +
            "channelid,'-',maxthreads,'-',timebuff,'-',note,'-',optionbuff) like ?1",nativeQuery = true)
    public List<VideoBuffhHistory> getOrderHistoryFilter(String key);

    @Query(value = "Select *\n" +
            "from videobuffhhistory where CONCAT(videoid,'-',videotitle,'-',\n" +
            "channelid,'-',maxthreads,'-',timebuff,'-',note,'-',optionbuff) like ?1 and user=?2",nativeQuery = true)
    public List<VideoBuffhHistory> getOrderHistoryFilter(String key,String user);


    @Query(value = "SELECT * FROM AccPremium.videobuffhhistory where insertdate>=1672272000000 and cancel!=1 and timecheck!=-1 and viewend is not null order by timecheck asc,enddate asc  limit ?1",nativeQuery = true)
    public List<VideoBuffhHistory> getVideoCheckBH(Integer limit);

    @Query(value = "SELECT * FROM AccPremium.videobuffhhistory where videoid=?1 and " +
            "id in( SELECT  * FROM (SELECT  MAX(id) FROM videobuffhhistory where user!='baohanh01@gmail.com' group by videoid) as p) limit 1",nativeQuery = true)
    public List<VideoBuffhHistory> getVideoBHByVideoId(String videoid);


    @Modifying
    @Transactional
    @Query(value = "update videobuffhhistory set timecheck=-1 where timecheck!=-1 and id not in( SELECT  * FROM (SELECT  MAX(id) FROM videobuffhhistory group by videoid) as p) and cancel!=1",nativeQuery = true)
    public Integer updatetimchecknomaxid();


    @Modifying
    @Transactional
    @Query(value = "update videobuffhhistory set viewend=?1 where videoid=?2",nativeQuery = true)
    public Integer updateviewend(Integer viewend,String videoid);

    @Query(value = "SELECT videoid FROM videobuffhhistory where viewend is null and round((UNIX_TIMESTAMP()-enddate/1000)/60/60)>=5 limit 50",nativeQuery = true)
    public List<String> getOrderHistorythan5h();
}
