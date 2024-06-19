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
            " left join service s on b.service=s.service_id where user=?1\n" +
            " b.service IS NOT NULL and round((UNIX_TIMESTAMP()-b.add_time/1000)/60/60/24)<=10 order by b.add_time desc",nativeQuery = true)
    public List<BalanceShow> getAllBalance(String user);
}
