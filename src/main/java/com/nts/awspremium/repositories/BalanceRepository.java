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
            "FROM balance\n" +
            "WHERE DATE_FORMAT(FROM_UNIXTIME((time+12*60*60*1000)/ 1000), '%Y-%m-%d') >= NOW() - INTERVAL 1 DAY\n" +
            "  AND DATE_FORMAT(FROM_UNIXTIME((time+12*60*60*1000)/ 1000), '%Y-%m-%d') < NOW() and balance<0 and service in(select service from service where geo='vn')",nativeQuery = true)
    public Float getAllBalanceVNNow();

    @Query(value = "SELECT ROUND(-sum(balance),2)\n" +
            "FROM balance\n" +
            "WHERE DATE_FORMAT(FROM_UNIXTIME((time+12*60*60*1000)/ 1000), '%Y-%m-%d') >= NOW() - INTERVAL 1 DAY\n" +
            "  AND DATE_FORMAT(FROM_UNIXTIME((time+12*60*60*1000)/ 1000), '%Y-%m-%d') < NOW() and balance<0 and service in(select service from service where geo='us')",nativeQuery = true)
    public Float getAllBalanceUSNow();
}
