package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Admin;
import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.BalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BalanceRepository extends JpaRepository<Balance,Long> {
    @Query(value = "Select id,balance,time,user,totalblance,balance.note,balance.service,service.geo from balance left join service on balance.service=service.service where balance.service IS NOT NULL and round((UNIX_TIMESTAMP()-time/1000)/60/60/24)<=10 order by time desc",nativeQuery = true)
    public List<BalanceHistory> getAllBalance();
    @Query(value = "Select id,balance,time,user,totalblance,balance.note,balance.service,service.geo from balance left join service on balance.service=service.service where balance.service IS NOT NULL and user=?1 and round((UNIX_TIMESTAMP()-time/1000)/60/60/24)<=10 order by time desc",nativeQuery = true)
    public List<BalanceHistory> getAllBalance(String user);

    @Query(value = "Select * from balance where balance<0  order by id desc limit 1 ",nativeQuery = true)
    public List<Balance> getfluctuationsNow();

    @Query(value = "Select * from balance where balance<0  order by id desc limit 1 ",nativeQuery = true)
    public Balance getBalanceByMaxId();

    @Query(value = "Select sum(balance) from balance where balance<0 and round((UNIX_TIMESTAMP()-time/1000)/60)<=5  order by id desc limit 1 ",nativeQuery = true)
    public Float getfluctuations5M();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00')\n" +
            "               AND  balance<0 and service in(select service from service where geo='vn')",nativeQuery = true)
    public Float getAllBalanceVNNow();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00')\n" +
            "               AND  balance<0 and service in(select service from service where geo='kr')",nativeQuery = true)
    public Float getAllBalanceKRNow();


    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00')\n" +
            "               AND  balance<0 and service in(select service from service where geo='us')",nativeQuery = true)
    public Float getAllBalanceUSNow();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00')\n" +
            "               AND  balance<0 and service in(select service from service where category='Youtube | Views' and geo='vn')",nativeQuery = true)
    public Float getAllBalanceVNNow1DG();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00')\n" +
            "               AND  balance<0 and service in(select service from service where category='Youtube | Views' and geo='us')",nativeQuery = true)
    public Float getAllBalanceUSNow1DG();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00')\n" +
            "               AND  balance<0 and service in(select service from service where category='Youtube | Views' and geo='kr')",nativeQuery = true)
    public Float getAllBalanceKRNow1DG();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00')\n" +
            "               AND  balance<0 and service in(select service from service where category='Youtube | Comments' and geo='vn')",nativeQuery = true)
    public Float getAllBalanceVNNow1DGCMT();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00')\n" +
            "               AND  balance<0 and service in(select service from service where category='Youtube | Comments' and geo='us')",nativeQuery = true)
    public Float getAllBalanceUSNow1DGCMT();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00')\n" +
            "               AND  balance<0 and service in(select service from service where category='Youtube | Comments' and geo='kr')",nativeQuery = true)
    public Float getAllBalanceKRNow1DGCMT();


    @Query(value = "SELECT DATE(FROM_UNIXTIME((balance.time / 1000), '%Y-%m-%d %H:%i:%s') + INTERVAL (7-(SELECT TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)) hour) AS date, \n" +
            "       ROUND(-sum(balance),2),count(*) \n" +
            "FROM balance \n" +
            "WHERE balance < 0 and service is  not null and user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')<DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00') \n" +
            "GROUP BY date \n" +
            "ORDER BY date DESC \n" +
            "LIMIT 7;",nativeQuery = true)
    public List<String> Getbalance7day();

    @Query(value = "SELECT DATE(FROM_UNIXTIME((balance.time / 1000), '%Y-%m-%d %H:%i:%s') + INTERVAL (7-(SELECT TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)) hour) AS date, \n" +
            "       ROUND(-sum(balance),2),count(*) \n" +
            "FROM balance \n" +
            "WHERE balance > 0 and service is  not null and user in(select username from admin where role='ROLE_USER') and FROM_UNIXTIME((time/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')<DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 00-00-00') \n" +
            "GROUP BY date \n" +
            "ORDER BY date DESC \n" +
            "LIMIT 7;",nativeQuery = true)
    public List<String> GetbalanceSub7day();

}
