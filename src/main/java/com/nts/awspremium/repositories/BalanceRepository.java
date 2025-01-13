package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.BalanceShow;
import com.nts.awspremium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BalanceRepository extends JpaRepository<Balance,Long> {
    @Query(value = "call update_balance(?1,?2)",nativeQuery = true)
    public Float update_Balance(Float a,String username);

    @Query(value = "Select b.id,b.balance,b.add_time,b.user,b.total_blance,b.note,b.service,s.platform,s.task from balance b\n" +
            " left join service s on b.service=s.service_id where\n" +
            " b.service IS NOT NULL and round((UNIX_TIMESTAMP()-b.add_time/1000)/60/60/24)<=10 order by b.add_time desc",nativeQuery = true)
    public List<BalanceShow> getAllBalance();
    @Query(value = "Select b.id,b.balance,b.add_time,b.user,b.total_blance,b.note,b.service,s.platform,s.task from balance b\n" +
            " left join service s on b.service=s.service_id where user=?1 and\n" +
            " b.service IS NOT NULL and round((UNIX_TIMESTAMP()-b.add_time/1000)/60/60/24)<=10 order by b.add_time desc",nativeQuery = true)
    public List<BalanceShow> getAllBalance(String user);

    @Query(value = "SELECT DATE(FROM_UNIXTIME((add_time / 1000), '%Y-%m-%d %H:%i:%s') + INTERVAL (7-(SELECT TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)) hour) AS date, \n" +
            "       ROUND(-sum(balance),2),count(*) \n" +
            "FROM balance \n" +
            "WHERE balance < 0 and service is  not null and user in(select username from user where role='ROLE_USER') and FROM_UNIXTIME((add_time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')<DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00') \n" +
            "GROUP BY date \n" +
            "ORDER BY date DESC \n" +
            "LIMIT 7;",nativeQuery = true)
    public List<String> get_Balance_7day();

    @Query(value = "SELECT DATE(FROM_UNIXTIME((add_time / 1000), '%Y-%m-%d %H:%i:%s') + INTERVAL (7-(SELECT TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)) hour) AS date, \n" +
            "       ROUND(-sum(balance),2),count(*) \n" +
            "FROM balance \n" +
            "WHERE balance > 0 and service is  not null and user in(select username from user where role='ROLE_USER') and FROM_UNIXTIME((add_time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')<DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00') \n" +
            "GROUP BY date \n" +
            "ORDER BY date DESC \n" +
            "LIMIT 7;",nativeQuery = true)
    public List<String> get_Refund_7day();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                              FROM balance\n" +
            "                                WHERE user in(select username from user where role='ROLE_USER') and FROM_UNIXTIME((add_time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00')\n" +
            "                        AND  balance<0 ",nativeQuery = true)
    public Float getAllBalanceNow();

    @Query(value = "SELECT * from  balance WHERE balance<0 and service IS NOT NULL  order by id desc limit 1",nativeQuery = true)
    public Balance getfluctuationsNow();

    @Query(value = "Select sum(balance) from balance where balance<0 and round((UNIX_TIMESTAMP()-add_time/1000)/60)<=5  order by id desc limit 1 ",nativeQuery = true)
    public Float getfluctuations5M();
}
