package com.nts.awspremium.repositories;

import com.nts.awspremium.model.ChannelTikTokHistory;
import com.nts.awspremium.model.WebTrafficHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChannelTikTokHistoryRepository extends JpaRepository<ChannelTikTokHistory,Long> {

    @Query(value = "SELECT * from webtraffichistory where round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=10 order by enddate desc",nativeQuery = true)
    public List<WebTrafficHistory> getWebTrafficHistories();

    @Query(value = "SELECT * from webtraffichistory where user=?1 and round((UNIX_TIMESTAMP()-enddate/1000)/60/60/24)<=10 order by enddate desc",nativeQuery = true)
    public List<WebTrafficHistory> getWebTrafficHistories(String user);

    @Query(value = "SELECT count(*) from webtraffichistory where orderid=?1 and token=?2 ",nativeQuery = true)
    public Integer checkTrueByOrderIdAndToken(Long orderid,String token);

    @Query(value = "SELECT orderid from webtraffichistory where link=?1 limit 1",nativeQuery = true)
    public Long getOrderIdWebTrafficHistoryByLink(String link);
    @Query(value = "SELECT * from channel_tiktok_history where orderid=?1",nativeQuery = true)
    public ChannelTikTokHistory getChannelTikTokHistoriesById(Long orderid);

    @Query(value = "SELECT * from channel_tiktok_history where orderid in (?1)",nativeQuery = true)
    public List<ChannelTikTokHistory> getChannelTikTokHistoriesListById(List<String> list_orderid);
}
