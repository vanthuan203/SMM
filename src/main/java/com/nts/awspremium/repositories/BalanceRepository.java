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

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE time>=UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000\n" +
            "               AND  balance<0 and service in(select service from service where geo='vn')",nativeQuery = true)
    public Float getAllBalanceVNNow();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "                    FROM balance\n" +
            "                     WHERE time>=UNIX_TIMESTAMP(CONVERT_TZ(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 hour),'%Y-%m-%d'),@@session.time_zone,'+7:00')) * 1000\n" +
            "               AND  balance<0 and service in(select service from service where geo='us')",nativeQuery = true)
    public Float getAllBalanceUSNow();
}
