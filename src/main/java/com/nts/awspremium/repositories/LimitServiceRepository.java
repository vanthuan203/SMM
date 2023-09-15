package com.nts.awspremium.repositories;

import com.nts.awspremium.model.LimitService;
import com.nts.awspremium.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LimitServiceRepository extends JpaRepository<LimitService,String> {
    @Query(value = "SELECT maxorder from limitservice where user=?1 and service=?2 limit 1",nativeQuery = true)
    public Integer getLimitPendingByServiceAndUser(String user,Integer service);

    @Query(value = "SELECT maxrunning from limitservice where user=?1 and service=?2 limit 1",nativeQuery = true)
    public Integer getLimitRunningByServiceAndUser(String user,Integer service);

    @Query(value = "SELECT * FROM  limitservice order by service asc",nativeQuery = true)
    public List<LimitService> getLimitServiceAll();

    @Query(value = "SELECT * FROM  limitservice where id=?1 limit 1",nativeQuery = true)
    public List<LimitService> getLimitById(Long id);

}