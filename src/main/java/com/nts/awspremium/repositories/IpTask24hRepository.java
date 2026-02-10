package com.nts.awspremium.repositories;

import com.nts.awspremium.model.IpTask24h;
import com.nts.awspremium.model.TiktokFollower24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface IpTask24hRepository extends JpaRepository<IpTask24h,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from ip_task_24h where update_time <= (UNIX_TIMESTAMP() - 24*60*60) * 1000",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select count(*) from ip_task_24h where id like ?1",nativeQuery = true)
    public Integer count_Task_24h_By_Ip(String ip);

    @Query(value = "select count(*) from ip_task_24h where id like ?1 and update_time >= (UNIX_TIMESTAMP() - ?2*60*60) * 1000",nativeQuery = true)
    public Integer count_Task_Hour_By_Ip(String ip,Integer hour);

    @Query(value = "select count(*) from ip_task_24h where id like ?1 and update_time >= (UNIX_TIMESTAMP() - ?2*60) * 1000",nativeQuery = true)
    public Integer count_Task_Minute_By_Ip(String ip,Integer minute);
}
