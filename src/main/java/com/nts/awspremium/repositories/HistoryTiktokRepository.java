package com.nts.awspremium.repositories;

import com.nts.awspremium.model.HistoryTikTok;
import com.nts.awspremium.model.HistoryTraffic;
import com.nts.awspremium.model.VpsRunning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HistoryTiktokRepository extends JpaRepository<HistoryTikTok,String> {
    @Query(value = "SELECT count(*) FROM history_tiktok where username=?1",nativeQuery = true)
    public Integer checkUsername(String username);
    @Modifying
    @Transactional
    @Query(value = "UPDATE history_tiktok SET vps=?1 where device_id=?2",nativeQuery = true)
    public void updateVPSByDevice(String vps,String device_id);
    @Modifying
    @Transactional
    @Query(value = "UPDATE history_tiktok SET running=0,orderid=0 where username=?1",nativeQuery = true)
    public Integer resetThreadByUsername(String username);
    @Query(value = "select device_id as vps,time as timeget,sum as total from (select max(timeget) as time,device_id,vps,sum(running) as sum from history_tiktok where  timeget!=0 and vps=?1  group by device_id order by sum desc) as t  ;",nativeQuery = true)
    public List<VpsRunning> getDeviceRunningByVPS(String vps);

    @Query(value = "SELECT vps,timeget,count(*) as total FROM history_tiktok where running>0 group by vps order by total desc",nativeQuery = true)
    public List<VpsRunning> getvpsrunning();
    @Query(value = "SELECT * FROM history_tiktok where id=?1 limit 1",nativeQuery = true)
    public List<HistoryTraffic> getHistoriesById(Long id);

    @Query(value = "SELECT timeget FROM AccPremium.history_tiktok where vps=?1 order by timeget desc limit 1;",nativeQuery = true)
    public Long getTimeGetByVPS(String vps);
    @Query(value = "SELECT * FROM history_tiktok where username=?1 limit 1",nativeQuery = true)
    public HistoryTikTok getHistoryTikTokByUsername(String username);

    @Query(value = "SELECT count(*) FROM history_tiktok where option_running=1 and vps=?1",nativeQuery = true)
    public Integer getHistoryFollowerTikTokByVPS(String vps);

    @Query(value = "SELECT count(*) FROM history_tiktok where option_running=1 and device_id=?1",nativeQuery = true)
    public Integer getHistoryFollowerTikTokByDeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE history_tiktok SET running=0,orderid=0 where vps=?1",nativeQuery = true)
    public Integer resetThreadByVps(String vps);

    @Modifying
    @Transactional
    @Query(value = "delete from history_tiktok where vps=?1",nativeQuery = true)
    public Integer deleteAllByVPS(String vps);

    @Modifying
    @Transactional
    @Query(value = "delete from history_tiktok where username=?1",nativeQuery = true)
    public Integer deleteHistoryTikTokByUsername(String vps);
    @Modifying
    @Transactional
    @Query(value = "update history_tiktok set running=0,orderid=0 where running>=1 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=60",nativeQuery = true)
    public Integer resetThreadcron();

    @Modifying
    @Transactional
    @Query(value = "update history_tiktok set option_running=1 where option_running=0 and username in (SELECT username FROM AccPremium.activity_tiktok where\n" +
            " FROM_UNIXTIME((time_update/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')<DATE_SUB(DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 0:0:0'),INTERVAL (select max_day_activity from setting_tiktok where id=1) DAY) group by username)",nativeQuery = true)
    public Integer updateOptionRunningFollower();

}
