package com.nts.awspremium.repositories;

import com.nts.awspremium.model.IpRegister;
import com.nts.awspremium.model.IpTask24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface IpRegisterRepository extends JpaRepository<IpRegister,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from ip_task_24h where round((UNIX_TIMESTAMP()-update_time/1000)/60/60)>24;",nativeQuery = true)
    public Integer deleteAllByThan24h();
    @Query(value = "select * from ip_register where id=?1 and platform=?2",nativeQuery = true)
    public IpRegister get_Ip_By_Ip_And_Platform(String ip,String platform);

    @Query(value = "select count(*) from ip_task_24h where id like ?1 and round((UNIX_TIMESTAMP()-update_time/1000)/60/60)<=?2",nativeQuery = true)
    public Integer count_Task_Hour_By_Ip(String ip,Integer hour);

    @Query(value = "select count(*) from ip_task_24h where id like ?1 and round((UNIX_TIMESTAMP()-update_time/1000)/60/60)<1",nativeQuery = true)
    public Integer count_Task_1h_By_Ip(String ip);
}
