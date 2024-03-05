package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Admin;
import com.nts.awspremium.model.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BalanceRepository extends JpaRepository<Balance,Long> {
    @Query(value = "Select * from balance where round((UNIX_TIMESTAMP()-time/1000)/60/60/24)<=10 order by id desc",nativeQuery = true)
    public List<Balance> getAllBalance();
    @Query(value = "Select * from balance where user=?1 and round((UNIX_TIMESTAMP()-time/1000)/60/60/24)<=10 order by id desc",nativeQuery = true)
    public List<Balance> getAllBalance(String user);

    @Query(value = "Select * from balance where balance<0  order by id desc limit 1 ",nativeQuery = true)
    public List<Balance> getfluctuationsNow();

    @Query(value = "Select sum(balance) from balance where balance<0 and round((UNIX_TIMESTAMP()-time/1000)/60)<=5  order by id desc limit 1 ",nativeQuery = true)
    public Float getfluctuations5M();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE time>=UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000\n" +
            "               AND  balance<0 and service in(select service from service where geo='vn')",nativeQuery = true)
    public Float getAllBalanceVNNow();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE time>=UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000\n" +
            "               AND  balance<0 and service in(select service from service where geo='kr')",nativeQuery = true)
    public Float getAllBalanceKRNow();


    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE time>=UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000\n" +
            "               AND  balance<0 and service in(select service from service where geo='us')",nativeQuery = true)
    public Float getAllBalanceUSNow();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user='1dg@gmail.com' and time>=UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000\n" +
            "               AND  balance<0 and service in(select service from service where geo='vn')",nativeQuery = true)
    public Float getAllBalanceVNNow1DG();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user='1dg@gmail.com' and time>=UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000\n" +
            "               AND  balance<0 and service in(select service from service where geo='us')",nativeQuery = true)
    public Float getAllBalanceUSNow1DG();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user='1dgcmt@gmail.com' and time>=UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000\n" +
            "               AND  balance<0 and service in(select service from service where geo='vn')",nativeQuery = true)
    public Float getAllBalanceVNNow1DGCMT();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE user='1dgcmt@gmail.com' and time>=UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000\n" +
            "               AND  balance<0 and service in(select service from service where geo='us')",nativeQuery = true)
    public Float getAllBalanceUSNow1DGCMT();


    @Query(value = "SELECT DATE(FROM_UNIXTIME((balance.time / 1000), '%Y-%m-%d %H:%i:%s') + INTERVAL (7-(SELECT TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)) hour) AS date, \n" +
            "       ROUND(-sum(balance),2),count(*) \n" +
            "FROM balance \n" +
            "WHERE balance < 0 and user in(select username from admin where role='ROLE_USER') and time<UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000 \n" +
            "GROUP BY date \n" +
            "ORDER BY date DESC \n" +
            "LIMIT 30;",nativeQuery = true)
    public List<String> Getbalance7day();

    @Query(value = "SELECT DATE(FROM_UNIXTIME((balance.time / 1000), '%Y-%m-%d %H:%i:%s') + INTERVAL (7-(SELECT TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)) hour) AS date, \n" +
            "       ROUND(-sum(balance),2),count(*) \n" +
            "FROM balance \n" +
            "WHERE balance > 0 and user in(select username from admin where role='ROLE_USER') and time<UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000 \n" +
            "GROUP BY date \n" +
            "ORDER BY date DESC \n" +
            "LIMIT 30;",nativeQuery = true)
    public List<String> GetbalanceSub7day();

}
