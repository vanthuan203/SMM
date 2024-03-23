package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ChannelTikTokHistory;
import com.nts.awspremium.model.WebTrafficHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChannelTikTokHistoryRepository extends JpaRepository<ChannelTikTokHistory,Long> {

    @Query(value = "SELECT * from channel_tiktok_history where round((UNIX_TIMESTAMP()-end_date/1000)/60/60/24)<=10 order by end_date desc",nativeQuery = true)
    public List<ChannelTikTokHistory> getOrderFollowerTiktokHistories();

    @Query(value = "SELECT * from channel_tiktok_history where user=?1 and round((UNIX_TIMESTAMP()-end_date/1000)/60/60/24)<=10 order by end_ate desc",nativeQuery = true)
    public List<ChannelTikTokHistory> getOrderFollowerTiktokHistories(String user);

    @Query(value = "SELECT count(*) from webtraffichistory where orderid=?1 and token=?2 ",nativeQuery = true)
    public Integer checkTrueByOrderIdAndToken(Long orderid,String token);

    @Query(value = "SELECT orderid from webtraffichistory where link=?1 limit 1",nativeQuery = true)
    public Long getOrderIdWebTrafficHistoryByLink(String link);
    @Query(value = "SELECT * from channel_tiktok_history where orderid=?1",nativeQuery = true)
    public ChannelTikTokHistory getChannelTikTokHistoriesById(Long orderid);

    @Query(value = "SELECT * from channel_tiktok_history where orderid in (?1)",nativeQuery = true)
    public List<ChannelTikTokHistory> getChannelTikTokHistoriesListById(List<String> list_orderid);

    @Query(value = "SELECT * from channel_tiktok_history where orderid in (?1) and user=?2",nativeQuery = true)
    public List<ChannelTikTokHistory> getChannelTikTokHistoriesListById(List<String> list_orderid,String user);

    @Query(value = "SELECT orderid,tiktok_id,price,follower_order,follower_start,follower_total,service,insert_date,time_start,cancel,end_date,time_check_refill,follower_end,user,note from channel_tiktok_history where tiktok_id=?1 and orderid<?2 order by orderid desc limit 1",nativeQuery = true)
    public String getInfoSumOrderByTiktokId(String tiktok_id,Long orderid);

    @Query(value = "SELECT orderid,tiktok_id,price,follower_order,follower_start,follower_total,service,insert_date,time_start,cancel,end_date,time_check_refill,follower_end,user,note from channel_tiktok_history where tiktok_id=?1 and orderid<?2 and user=?3 order by orderid desc limit 1",nativeQuery = true)
    public String getInfoSumOrderByTiktokId(String tiktok_id,Long orderid,String user);
}
